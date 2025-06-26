package com.lambda.security.exception;

import com.lambda.security.LoginCode;

public class AccountExpireException extends AuthenticationException {
    public AccountExpireException(String message) {
        super(LoginCode.CODE_20003, message);
    }
}
