package com.jingfang.security.web.authentication.handler;

import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.enums.LoginType;
import com.jingfang.security.handler.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DefaultLogoutHandler
 *
 * @author jpjoo
 */
public class DefaultLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        StpLogic adminStpLogic = LoginType.ADMIN.getStpLogic();
        if (adminStpLogic.isLogin()) {
            adminStpLogic.logout();
        }
        StpLogic userStpLogic = LoginType.USER.getStpLogic();
        if (userStpLogic.isLogin()) {
            userStpLogic.logout();
        }
    }
}
