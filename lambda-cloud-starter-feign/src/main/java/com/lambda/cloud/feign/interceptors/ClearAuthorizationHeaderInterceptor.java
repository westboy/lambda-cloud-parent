package com.lambda.cloud.feign.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.core.PriorityOrdered;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * @author westboy
 */
public class ClearAuthorizationHeaderInterceptor implements RequestInterceptor, PriorityOrdered {

    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(AUTHORIZATION);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
