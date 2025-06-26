package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

public class BadCredentialsException extends AuthenticationException {
    public BadCredentialsException(String message) {
        super(LoginErrorCode.CODE_20001, message);
    }
}
