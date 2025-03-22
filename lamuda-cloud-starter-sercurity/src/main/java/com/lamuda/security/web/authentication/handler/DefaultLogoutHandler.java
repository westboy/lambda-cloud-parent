package com.lamuda.security.web.authentication.handler;

import cn.dev33.satoken.stp.StpLogic;
import com.lamuda.cloud.core.principal.LoginUser;
import com.lamuda.cloud.core.principal.LoginType;
import com.lamuda.security.handler.LogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * DefaultLogoutHandler
 *
 * @author jpjoo
 */
public class DefaultLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        StpLogic stpLogic = LoginType.getActiveStpLogic();
        stpLogic.logout();
    }
}
