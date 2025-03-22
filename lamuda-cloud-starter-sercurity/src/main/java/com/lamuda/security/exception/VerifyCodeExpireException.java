package com.lamuda.security.exception;

/**
 * VerifyCodeException
 * @author Jin
 */
public class VerifyCodeExpireException extends AuthenticationException {

    public VerifyCodeExpireException(String message) {
        super(message);
    }
}
