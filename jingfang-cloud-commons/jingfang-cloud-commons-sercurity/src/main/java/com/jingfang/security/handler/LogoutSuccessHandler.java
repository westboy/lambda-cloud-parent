package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.LoginUser;

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