package com.jingfang.security.web.authentication.handler;

import com.jingfang.cloud.core.principal.Principal;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.cloud.web.RequestTimeHolder;
import com.jingfang.security.handler.LogoutSuccessHandler;
import com.jingfang.security.web.events.UserLogoutEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler, ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException, ServletException {
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        applicationEventPublisher.publishEvent(new UserLogoutEvent(principal, cast));
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
