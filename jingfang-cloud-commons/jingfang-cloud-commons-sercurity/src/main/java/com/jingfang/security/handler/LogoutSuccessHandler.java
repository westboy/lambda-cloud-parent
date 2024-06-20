package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * LogoutSuccessHandler
 *
 * @author jpjoo
 */
public interface LogoutSuccessHandler {
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException, ServletException;
}