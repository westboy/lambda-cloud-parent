package com.lambda.cloud.lucene.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LuceneManagerFactoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void replaceDocumentsIsVisibleImmediatelyAndRemovesPreviousChunks() throws Exception {
        try (LuceneManagerFactory factory = new LuceneManagerFactory(tempDirectory, SmartChineseAnalyzer::new)) {
            LuceneManager manager = factory.get("rag/kb-1");
            manager.replaceDocuments(
                    new Term("docId", "doc-1"), List.of(document("doc-1", "旧版本说明"), document("doc-1", "按下复位按钮恢复设备")));

            assertThat(manager.search(manager.parseQuery("复位按钮", "content"), 10))
                    .hasSize(1);

            manager.replaceDocuments(new Term("docId", "doc-1"), List.of(document("doc-1", "新版本仅支持断电重启")));

            assertThat(manager.search(manager.parseQuery("复位按钮", "content"), 10))
                    .isEmpty();
            assertThat(manager.search(manager.parseQuery("断电重启", "content"), 10))
                    .hasSize(1);
        }
    }

    @Test
    void logicalIndexesAreIsolatedAndDeleteStaysInsideRoot() throws Exception {
        try (LuceneManagerFactory factory = new LuceneManagerFactory(tempDirectory, SmartChineseAnalyzer::new)) {
            LuceneManager first = factory.get("rag/kb-1");
            LuceneManager second = factory.get("rag/kb-2");
            first.addDocuments(List.of(document("doc-1", "第一知识库专有内容")));

            assertThat(first.search(first.parseQuery("专有内容", "content"), 10)).hasSize(1);
            assertThat(second.search(second.parseQuery("专有内容", "content"), 10)).isEmpty();

            Path firstDirectory = first.getDirectoryPath();
            factory.delete("rag/kb-1");
            assertThat(Files.exists(firstDirectory)).isFalse();
            assertThat(Files.exists(second.getDirectoryPath())).isTrue();
        }
    }

    @Test
    void pathTraversalIndexNameIsRejected() throws Exception {
        try (LuceneManagerFactory factory = new LuceneManagerFactory(tempDirectory, SmartChineseAnalyzer::new)) {
            assertThatThrownBy(() -> factory.get("../outside")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(
                            () -> factory.get(tempDirectory.resolve("absolute").toString()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static Document document(String docId, String content) {
        Document document = new Document();
        document.add(new StringField("docId", docId, Field.Store.YES));
        document.add(new TextField("content", content, Field.Store.YES));
        return document;
    }
}
