package com.lambda.security.exception;

import cn.dev33.satoken.exception.SaTokenException;

/**
 * AuthenticationException
 *
 * @author Jin
 */
public class AuthenticationException extends SaTokenException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
