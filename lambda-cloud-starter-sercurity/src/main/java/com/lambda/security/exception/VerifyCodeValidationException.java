package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * VerifyCodeException
 *
 * @author Jin
 */
public class VerifyCodeValidationException extends AuthenticationException {

    public VerifyCodeValidationException(String message) {
        super(LoginErrorCode.CODE_20004, message);
    }
}
