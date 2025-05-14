package com.lambda.cloud.oss.exception;

public class OssException extends RuntimeException {
    public OssException(String message) {
        super(message);
    }

    public OssException(String message, Throwable cause) {
        super(message, cause);
    }
}
