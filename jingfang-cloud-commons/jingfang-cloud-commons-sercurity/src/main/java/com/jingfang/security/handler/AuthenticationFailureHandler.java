package com.jingfang.security.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthenticationFailureHandler
 *
 * @author jpjoo
 */
public interface AuthenticationFailureHandler {
    /**
     * 认证失败
     *
     * @param request
     * @param response
     * @param exception
     * @throws IOException
     * @throws ServletException
     */
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException, ServletException;
}
