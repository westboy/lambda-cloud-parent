package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * LogoutSuccessHandler
 *
 * @author jpjoo
 */
public interface LogoutSuccessHandler {

    /**
     * 退出成功
     *
     * @param request
     * @param response
     * @param loginUser
     * @throws IOException
     * @throws ServletException
     */
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws IOException, ServletException;
}