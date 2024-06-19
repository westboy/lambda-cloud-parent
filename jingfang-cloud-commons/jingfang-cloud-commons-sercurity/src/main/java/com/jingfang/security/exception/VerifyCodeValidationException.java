package com.jingfang.security.exception;

/**
 * VerifyCodeException
 * @author Jin
 */
public class VerifyCodeValidationException extends RuntimeException {

    public VerifyCodeValidationException(String message) {
        super(message);
    }
}
