package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.RequestTimeHolder;
import com.lambda.security.events.UserLogoutEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认登录成功处理器
 *
 * @author jpjoo
 */
@SuppressWarnings("all")
public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler, ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws IOException, ServletException {
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        applicationEventPublisher.publishEvent(new UserLogoutEvent(loginUser, cast));
        if (WebHttpUtils.isAjaxRequest(request)) {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().flush();
        } else {
            WebHttpUtils.getRedirectParameter(request, "/");
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
