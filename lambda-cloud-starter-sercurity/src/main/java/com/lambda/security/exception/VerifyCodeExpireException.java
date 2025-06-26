package com.lambda.security.exception;

import com.lambda.security.LoginCode;

/**
 * VerifyCodeException
 * @author Jin
 */
public class VerifyCodeExpireException extends AuthenticationException {

    public VerifyCodeExpireException(String message) {
        super(LoginCode.CODE_20005, message);
    }
}
