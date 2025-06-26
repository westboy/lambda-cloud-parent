package com.lambda.security.exception;

import com.lambda.security.LoginCode;

public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String message) {
        super(LoginCode.CODE_20002, message);
    }
}
