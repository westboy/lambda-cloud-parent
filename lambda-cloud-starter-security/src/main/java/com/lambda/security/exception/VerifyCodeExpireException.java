package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * VerifyCodeException
 * @author Jin
 */
public class VerifyCodeExpireException extends AuthenticationException {

    public VerifyCodeExpireException(String message) {
        super(LoginErrorCode.CODE_20005, message);
    }
}
