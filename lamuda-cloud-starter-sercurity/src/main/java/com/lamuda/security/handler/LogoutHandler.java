package com.lamuda.security.handler;

import com.lamuda.cloud.core.principal.LoginUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LogoutHandler
 *
 * @author jpjoo
 */
public interface LogoutHandler {
    /**
     * 退出处理器
     *
     * @param request  request
     * @param response response
     * @param loginUser loginUser
     */
    void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser);
}