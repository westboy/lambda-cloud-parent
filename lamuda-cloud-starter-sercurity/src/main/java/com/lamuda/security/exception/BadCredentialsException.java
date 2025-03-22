package com.lamuda.security.exception;

public class BadCredentialsException extends AuthenticationException{
    public BadCredentialsException(String message) {
        super(message);
    }
}
