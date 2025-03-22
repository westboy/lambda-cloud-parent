package com.lamuda.security.web.authentication.handler;

import com.lamuda.cloud.core.principal.LoginUser;
import com.lamuda.cloud.mvc.WebHttpUtils;
import com.lamuda.cloud.web.RequestTimeHolder;
import com.lamuda.security.handler.LogoutSuccessHandler;
import com.lamuda.security.web.events.UserLogoutEvent;
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

    @SuppressWarnings("all")
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
