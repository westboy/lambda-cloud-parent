package com.lambda.cloud.lucene.exception;

/** Lucene 逻辑索引创建、关闭或删除失败。 */
public class LuceneOperationException extends RuntimeException {

    public LuceneOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
