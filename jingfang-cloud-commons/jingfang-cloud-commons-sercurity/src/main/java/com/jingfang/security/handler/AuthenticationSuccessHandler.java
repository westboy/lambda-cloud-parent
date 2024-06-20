package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.Principal;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthenticationFailureHandler
 *
 * @author jpjoo
 */
public interface AuthenticationSuccessHandler {
    default void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Principal principal) throws IOException, ServletException {
        this.onAuthenticationSuccess(request, response, principal);
        chain.doFilter(request, response);
    }

    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException, ServletException;
}
