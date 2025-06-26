package com.lambda.security.exception;

import com.lambda.security.LoginCode;

public class BadCredentialsException extends AuthenticationException {
    public BadCredentialsException(String message) {
        super(LoginCode.CODE_20001, message);
    }
}
