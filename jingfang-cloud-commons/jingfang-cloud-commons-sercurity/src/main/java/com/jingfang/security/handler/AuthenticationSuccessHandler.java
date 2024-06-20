package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.Principal;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     * @param principal
     * @throws IOException
     * @throws ServletException
     */
    default void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Principal principal) throws IOException, ServletException {
        this.onAuthenticationSuccess(request, response, principal);
        chain.doFilter(request, response);
    }

    /**
     * 认证成功
     *
     * @param request
     * @param response
     * @param principal
     * @throws IOException
     * @throws ServletException
     */
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException, ServletException;
}
