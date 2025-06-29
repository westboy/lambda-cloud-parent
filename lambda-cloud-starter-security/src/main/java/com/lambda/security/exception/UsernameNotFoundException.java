package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

public class UsernameNotFoundException extends AuthenticationException {
    public UsernameNotFoundException(String message) {
        super(LoginErrorCode.CODE_20001, message);
    }
}
