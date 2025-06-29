package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String message) {
        super(LoginErrorCode.CODE_20002, message);
    }
}
