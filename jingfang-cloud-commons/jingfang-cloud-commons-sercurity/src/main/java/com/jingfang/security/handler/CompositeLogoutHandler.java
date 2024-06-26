package com.jingfang.security.handler;

import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.utils.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * CompositeLogoutHandler
 *
 * @author jpjoo
 */
public final class CompositeLogoutHandler implements LogoutHandler {
    private final List<LogoutHandler> logoutHandlers;

    public CompositeLogoutHandler(LogoutHandler... logoutHandlers) {
        Assert.notEmpty(logoutHandlers, "LogoutHandlers are required");
        this.logoutHandlers = Arrays.asList(logoutHandlers);
    }

    public CompositeLogoutHandler(List<LogoutHandler> logoutHandlers) {
        Assert.notEmpty(logoutHandlers, "LogoutHandlers are required");
        this.logoutHandlers = logoutHandlers;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        for (LogoutHandler handler : this.logoutHandlers) {
            handler.logout(request, response, loginUser);
        }
    }
}