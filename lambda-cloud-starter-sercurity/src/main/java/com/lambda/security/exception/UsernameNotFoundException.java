package com.lambda.security.exception;

import com.lambda.security.LoginCode;

public class UsernameNotFoundException extends AuthenticationException {
    public UsernameNotFoundException(String message) {
        super(LoginCode.CODE_20001, message);
    }
}
