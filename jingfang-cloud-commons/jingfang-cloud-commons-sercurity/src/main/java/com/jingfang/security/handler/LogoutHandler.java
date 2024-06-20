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
    void logout(HttpServletRequest request, HttpServletResponse response, Principal principal);
}