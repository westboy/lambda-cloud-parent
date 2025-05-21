package com.lambda.security.handler.impl;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;

import com.lambda.security.handler.LogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
