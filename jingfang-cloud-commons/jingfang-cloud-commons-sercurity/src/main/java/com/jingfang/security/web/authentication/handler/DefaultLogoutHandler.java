package com.jingfang.security.web.authentication.handler;

import com.jingfang.cloud.core.principal.Principal;
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
    public void logout(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        LoginType.ADMIN.getStpLogic().logout();
        LoginType.USER.getStpLogic().logout();
    }
}
