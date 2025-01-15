package com.jingfang.security.web.authentication.handler;

import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.principal.LoginType;
import com.jingfang.security.handler.LogoutHandler;

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
