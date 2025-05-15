
package com.lambda.cloud.lucene.manager;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lambda.cloud.lucene.model.AbstractIndexObject;
import com.lambda.cloud.lucene.utils.IndexObjectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LuceneManager
 *
 * @author Jin
 */
@Setter
@Getter
@Slf4j
public class LuceneManager {

    private Path directoryPath = null;
    private Analyzer analyzer = null;

    /**
     * 创建索引
     *
     * @param abstractIndexObject AbstractIndexObject
     */
    @SneakyThrows
    public void create(AbstractIndexObject abstractIndexObject) {
        IndexWriter indexWriter = getIndexWriter();
        try {
            Long result = indexWriter.addDocument(IndexObjectUtil.indexObjectToDocument(abstractIndexObject));
            log.info("====[ 创建索引: {} ]====", result);
            indexWriter.commit();
        } catch (Exception e) {
            log.error("创建索引失败！", e);
            indexWriter.rollback();
        } finally {
            indexWriter.close();
        }

    }

    /**
     * 更新索引
     *
     * @param abstractIndexObject AbstractIndexObject
     */
    public void update(AbstractIndexObject abstractIndexObject) throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        try {
            Long result = indexWriter.updateDocument(new Term("id", abstractIndexObject.id()), IndexObjectUtil.indexObjectToDocument(abstractIndexObject));
            log.info("====[ 更新索引: {} ]====", result);
            indexWriter.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            indexWriter.rollback();
        } finally {
            indexWriter.close();
        }

    }

    /**
     * 删除索引
     *
     * @param id String
     */
    public void delete(String id) throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        try {
            Long result = indexWriter.deleteDocuments(new Term("id", id));
            log.info("====[ 删除索引: {} ]====", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            indexWriter.rollback();
        } finally {
            indexWriter.close();
        }
    }

    /**
     * 删除全部索引
     *
     */
    public void deleteAll() throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        try {
            Long result = indexWriter.deleteAll();
            log.info("====[ 清空索引: {} ]====", result);
            //清空回收站
            indexWriter.forceMergeDeletes();
        } catch (Exception e) {
            log.error(e.getMessage());
            indexWriter.rollback();
        } finally {
            indexWriter.close();
        }
    }


    /**
     * 检索分页
     *
     * @param current
     * @param size
     * @param keyword
     * @param fields
     * @return page
     * @throws IOException
     */
    public <T extends AbstractIndexObject> IPage<T> page(String keyword, Integer current, Integer size, Class<T> clazz, String... fields) throws IOException {
        IndexReader indexReader = getIndexReader();
        IPage<T> page = new Page<>(current, size);
        try {
            List<T> searchResultList = new ArrayList<>();
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Query query = getQuery(keyword, getAnalyzer(), fields);
            //根据页码和分页大小获取上一次的最后一个ScoreDoc
            ScoreDoc lastScoreDoc = getLastScoreDoc(current, size, query, indexSearcher);
            TopDocs topDocs = indexSearcher.searchAfter(lastScoreDoc, query, size);
            page.setTotal(topDocs.totalHits);
            //遍历转换
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                searchResultList.add(IndexObjectUtil.documentToIndexObject(getAnalyzer(), getHighlighter(query), document, scoreDoc.score, clazz));
            }
            //根据相似分数排序
            Collections.sort(searchResultList);
            page.setRecords(searchResultList);
            return page;
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            indexReader.close();
        }
        return page;
    }


    /**
     * getIndexWriter
     *
     * @return
     * @throws IOException
     */
    IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(getAnalyzer());
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(FSDirectory.open(getDirectoryPath()), indexWriterConfig);
    }

    /**
     * getIndexReader
     *
     * @return
     * @throws IOException
     */
    IndexReader getIndexReader() throws IOException {
        return DirectoryReader.open(FSDirectory.open(getDirectoryPath()));
    }

    /**
     * 根据页码和分页大小获取上一次的最后一个ScoreDoc
     *
     * @param pageNumber
     * @param pageSize
     * @param query
     * @param searcher
     * @return
     * @throws IOException
     */
    ScoreDoc getLastScoreDoc(Integer pageNumber, Integer pageSize, Query query, IndexSearcher searcher) throws IOException {
        if (ObjectUtil.equal(pageNumber, 1)) {
            return null;
        }
        int total = pageSize * (pageNumber - 1);
        TopDocs topDocs = searcher.search(query, total);
        return topDocs.scoreDocs[total - 1];
    }

    /**
     * getQuery
     *
     * @param query
     * @param analyzer
     * @param fields
     * @return
     * @throws ParseException
     */
    Query getQuery(String query, Analyzer analyzer, String... fields) throws ParseException {
        BooleanQuery.setMaxClauseCount(32768);
        query = QueryParser.escape(query);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        return parser.parse(query);
    }

    /**
     * 设置字符串高亮
     *
     * @param query
     * @return
     */
    Highlighter getHighlighter(Query query) {
        QueryScorer scorer = new QueryScorer(query);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Highlighter highlighter = new Highlighter(htmlFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter);
        return highlighter;
    }

}
