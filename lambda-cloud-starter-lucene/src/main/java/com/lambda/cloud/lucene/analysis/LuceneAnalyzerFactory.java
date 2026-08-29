package com.lambda.cloud.lucene.analysis;

import org.apache.lucene.analysis.Analyzer;

/** 为每个逻辑索引创建独立分词器，允许下游通过 Bean 覆盖默认实现。 */
@FunctionalInterface
public interface LuceneAnalyzerFactory {

    Analyzer create();
}
