package com.lambda.security.web.form;

import com.lambda.security.exception.AuthenticationException;

public interface FormLoginValidator {
    boolean support(FormLoginContext context);

    void validate(FormLoginContext context) throws AuthenticationException;
}
