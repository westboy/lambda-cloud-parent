package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LogoutHandler
 *
 * @author jpjoo
 */
public interface LogoutHandler {
    /**
     * 退出处理器
     *
     * @param request
     * @param response
     * @param principal
     */
    void logout(HttpServletRequest request, HttpServletResponse response, Principal principal);
}