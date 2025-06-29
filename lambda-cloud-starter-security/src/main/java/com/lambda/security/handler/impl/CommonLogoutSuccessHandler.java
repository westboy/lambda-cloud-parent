package com.lambda.security.handler.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.RequestTimeHolder;
import com.lambda.security.events.UserLogoutEvent;
import com.lambda.security.handler.LogoutSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;

/**
 * 默认登录成功处理器
 *
 * @author jpjoo
 */
@SuppressWarnings("all")
public class CommonLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException, ServletException {
        if (WebHttpUtils.isAjaxRequest(request)) {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().flush();
        } else {
            WebHttpUtils.getRedirectParameter(request, "/");
        }
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        SpringUtil.publishEvent(new UserLogoutEvent(loginUser, cast));
    }
}
