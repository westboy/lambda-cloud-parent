package com.lambda.security.exception;

public class UsernameNotFoundException extends AuthenticationException {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
