package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证成功处理器
 *
 * @author jpjoo
 */
public interface AuthenticationSuccessHandler {

    /**
     * 认证成功默认方法
     *
     * @param request
     * @param response
     * @param chain
     * @param loginUser
     * @throws IOException
     * @throws ServletException
     */
    default void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser) throws IOException, ServletException {
        this.onAuthenticationSuccess(request, response, loginUser);
        chain.doFilter(request, response);
    }

    /**
     * 认证成功
     *
     * @param request
     * @param response
     * @param loginUser
     * @throws IOException
     * @throws ServletException
     */
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws IOException, ServletException;
}
