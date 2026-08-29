package com.lambda.cloud.lucene.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.lucene.document.Document;

/** Lucene 检索命中；document 只包含建索引时声明为 stored 的字段。 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record LuceneSearchHit(Document document, float score) {}
