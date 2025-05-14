package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;

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
     * @param request request
     * @param response response
     * @param loginUser loginUser
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws IOException, ServletException;
}
