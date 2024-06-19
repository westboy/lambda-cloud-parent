package com.jingfang.security.exception;

/**
 * VerifyCodeException
 * @author Jin
 */
public class VerifyCodeExpireException extends RuntimeException {

    public VerifyCodeExpireException(String message) {
        super(message);
    }
}
