package com.lambda.security.exception;

import com.lambda.security.LoginCode;

/**
 * VerifyCodeException
 *
 * @author Jin
 */
public class VerifyCodeValidationException extends AuthenticationException {

    public VerifyCodeValidationException(String message) {
        super(LoginCode.CODE_20004, message);
    }
}
