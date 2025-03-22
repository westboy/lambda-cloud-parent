package com.lamuda.security.exception;

/**
 * VerifyCodeException
 *
 * @author Jin
 */
public class VerifyCodeValidationException extends AuthenticationException {

    public VerifyCodeValidationException(String message) {
        super(message);
    }
}
