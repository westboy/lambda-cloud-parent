package com.lambda.cloud.lucene.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lambda.cloud.lucene.model.IndexObject;
import com.lambda.cloud.lucene.model.LuceneSearchHit;
import com.lambda.cloud.lucene.utils.IndexObjectUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 单个 Lucene 逻辑索引的生命周期管理器。
 *
 * <p>实例长期持有 {@link IndexWriter} 与 {@link SearcherManager}，写操作提交后同步刷新搜索器；关闭操作与在途读写互斥。
 */
public final class LuceneManager implements AutoCloseable {

    private static final int MAX_BOOLEAN_CLAUSES = 32768;

    private final Path directoryPath;
    private final Analyzer analyzer;
    private final Directory directory;
    private final IndexWriter indexWriter;
    private final SearcherManager searcherManager;
    private final Object writeMonitor = new Object();
    private final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();
    private final AtomicBoolean closed = new AtomicBoolean();

    public LuceneManager(Path directoryPath, Analyzer analyzer) throws IOException {
        this.directoryPath = directoryPath.toAbsolutePath().normalize();
        this.analyzer = analyzer;
        Directory openedDirectory = null;
        IndexWriter openedWriter = null;
        SearcherManager openedSearcherManager = null;
        try {
            openedDirectory = FSDirectory.open(this.directoryPath);
            IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
            writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            openedWriter = new IndexWriter(openedDirectory, writerConfig);
            openedSearcherManager = new SearcherManager(openedWriter, null);
        } catch (IOException | RuntimeException e) {
            closeOnInitializationFailure(openedSearcherManager, openedWriter, openedDirectory, analyzer, e);
            throw e;
        }
        this.directory = openedDirectory;
        this.indexWriter = openedWriter;
        this.searcherManager = openedSearcherManager;
    }

    public Path getDirectoryPath() {
        return directoryPath;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /** 兼容既有对象模型的新增入口。 */
    public void create(IndexObject indexObject) throws IOException {
        addDocuments(List.of(IndexObjectUtil.indexObjectToDocument(indexObject)));
    }

    /** 兼容既有对象模型的更新入口。 */
    public void update(IndexObject indexObject) throws IOException {
        updateDocument(new Term("id", indexObject.getId()), IndexObjectUtil.indexObjectToDocument(indexObject));
    }

    /** 兼容既有对象模型的删除入口。 */
    public void delete(String id) throws IOException {
        deleteDocuments(new Term("id", id));
    }

    public void addDocuments(List<Document> documents) throws IOException {
        if (documents.isEmpty()) {
            return;
        }
        write(() -> indexWriter.addDocuments(documents));
    }

    public void updateDocument(Term selector, Document document) throws IOException {
        write(() -> indexWriter.updateDocument(selector, document));
    }

    /** 原子替换 selector 命中的全部文档；新集合为空时等同删除。 */
    public void replaceDocuments(Term selector, List<Document> documents) throws IOException {
        write(() -> {
            if (documents.isEmpty()) {
                indexWriter.deleteDocuments(selector);
            } else {
                indexWriter.updateDocuments(selector, documents);
            }
        });
    }

    public void deleteDocuments(Term selector) throws IOException {
        write(() -> indexWriter.deleteDocuments(selector));
    }

    public void deleteAll() throws IOException {
        write(indexWriter::deleteAll);
    }

    /** 使用当前索引分词器解析纯文本查询，调用方无需处理 QueryParser 语法。 */
    public Query parseQuery(String query, String... fields) throws ParseException {
        IndexSearcher.setMaxClauseCount(MAX_BOOLEAN_CLAUSES);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        return parser.parse(QueryParser.escape(query));
    }

    /** 返回已存储字段和 BM25 分数，结果顺序与 Lucene 排名一致。 */
    public List<LuceneSearchHit> search(Query query, int limit) throws IOException {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        return withSearcher(searcher -> {
            TopDocs topDocs = searcher.search(query, limit);
            List<LuceneSearchHit> hits = new ArrayList<>(topDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                hits.add(new LuceneSearchHit(searcher.storedFields().document(scoreDoc.doc), scoreDoc.score));
            }
            return hits;
        });
    }

    /** 兼容既有 MyBatis-Plus 分页入口。 */
    public <T extends IndexObject> IPage<T> page(
            String keyword, Integer current, Integer size, Class<T> clazz, String... fields) throws IOException {
        Query query;
        try {
            query = parseQuery(keyword, fields);
        } catch (ParseException e) {
            throw new IOException("Lucene query parse failed", e);
        }
        return withSearcher(searcher -> {
            IPage<T> page = new Page<>(current, size);
            ScoreDoc lastScoreDoc = getLastScoreDoc(current, size, query, searcher);
            TopDocs topDocs = searcher.searchAfter(lastScoreDoc, query, size);
            page.setTotal(topDocs.totalHits.value());
            List<T> results = new ArrayList<>(topDocs.scoreDocs.length);
            Highlighter highlighter = getHighlighter(query);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = searcher.storedFields().document(scoreDoc.doc);
                results.add(
                        IndexObjectUtil.documentToIndexObject(analyzer, highlighter, document, scoreDoc.score, clazz));
            }
            Collections.sort(results);
            page.setRecords(results);
            return page;
        });
    }

    private ScoreDoc getLastScoreDoc(int pageNumber, int pageSize, Query query, IndexSearcher searcher)
            throws IOException {
        if (pageNumber <= 1) {
            return null;
        }
        int preceding = Math.multiplyExact(pageSize, pageNumber - 1);
        TopDocs topDocs = searcher.search(query, preceding);
        return topDocs.scoreDocs.length < preceding ? null : topDocs.scoreDocs[preceding - 1];
    }

    private Highlighter getHighlighter(Query query) {
        QueryScorer scorer = new QueryScorer(query);
        SimpleSpanFragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<em>", "</em>"), scorer);
        highlighter.setTextFragmenter(fragmenter);
        return highlighter;
    }

    private void write(IoOperation operation) throws IOException {
        lifecycleLock.readLock().lock();
        try {
            ensureOpen();
            synchronized (writeMonitor) {
                operation.run();
                indexWriter.commit();
                searcherManager.maybeRefreshBlocking();
            }
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    private <T> T withSearcher(SearchOperation<T> operation) throws IOException {
        lifecycleLock.readLock().lock();
        try {
            ensureOpen();
            IndexSearcher searcher = searcherManager.acquire();
            try {
                return operation.apply(searcher);
            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Lucene index is closed: " + directoryPath);
        }
    }

    @Override
    public void close() throws IOException {
        lifecycleLock.writeLock().lock();
        try {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            IOException failure = null;
            failure = close(searcherManager, failure);
            failure = close(indexWriter, failure);
            failure = close(directory, failure);
            failure = close(analyzer, failure);
            if (failure != null) {
                throw failure;
            }
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    private static IOException close(AutoCloseable closeable, IOException failure) {
        try {
            closeable.close();
        } catch (Exception e) {
            IOException closeError = e instanceof IOException ioException
                    ? ioException
                    : new IOException("Lucene resource close failed", e);
            if (failure == null) {
                return closeError;
            }
            failure.addSuppressed(closeError);
        }
        return failure;
    }

    private static void closeOnInitializationFailure(
            SearcherManager searcherManager,
            IndexWriter indexWriter,
            Directory directory,
            Analyzer analyzer,
            Exception original) {
        for (AutoCloseable resource : new AutoCloseable[] {searcherManager, indexWriter, directory, analyzer}) {
            if (resource == null) {
                continue;
            }
            try {
                resource.close();
            } catch (Exception closeError) {
                original.addSuppressed(closeError);
            }
        }
    }

    @FunctionalInterface
    private interface IoOperation {
        void run() throws IOException;
    }

    @FunctionalInterface
    private interface SearchOperation<T> {
        T apply(IndexSearcher searcher) throws IOException;
    }
}
