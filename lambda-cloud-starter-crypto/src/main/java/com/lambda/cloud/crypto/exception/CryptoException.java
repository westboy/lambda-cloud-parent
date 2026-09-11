package com.lambda.cloud.crypto.exception;

/**
 * 加密组件异常
 *
 * @author Jin
 * @since 2026.1.1
 */
public class CryptoException extends RuntimeException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
