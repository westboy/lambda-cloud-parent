package com.lambda.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthenticationFailureHandler
 *
 * @author jpjoo
 */
public interface AuthenticationFailureHandler {
    /**
     * 认证失败
     *
     * @param request request
     * @param response response
     * @param exception exception
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception)
            throws IOException, ServletException;
}
