package com.lamuda.security.handler;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException, ServletException;
}
