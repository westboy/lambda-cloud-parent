package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

public class AccountExpireException extends AuthenticationException {
    public AccountExpireException(String message) {
        super(LoginErrorCode.CODE_20003, message);
    }
}
