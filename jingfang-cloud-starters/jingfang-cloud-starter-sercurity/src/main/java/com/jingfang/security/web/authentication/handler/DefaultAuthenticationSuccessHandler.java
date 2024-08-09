package com.jingfang.security.web.authentication.handler;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.cloud.web.RequestTimeHolder;
import com.jingfang.security.enums.LoginType;
import com.jingfang.security.handler.AuthenticationSuccessHandler;
import com.jingfang.security.web.events.UserLoginEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNullApi;

import java.io.IOException;

/**
 * 默认认证成功处理器
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler, ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    private final ObjectMapper objectMapper;

    public DefaultAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) throws IOException {
        //获取用户登录时的IP,需要Nginx做相关配置防止IP伪造
        String remoteAddr = JakartaServletUtil.getClientIP(request);
        //获取用户登录时的端口
        int remotePort = request.getRemotePort();
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        //发布登录时间
        applicationEventPublisher.publishEvent(new UserLoginEvent(loginUser, cast, remoteAddr, remotePort));
        //sa-token 登录
        //登录设备
        String device = (String) request.getAttribute("device");
        //多用户类型支持
        String loginType = (String) request.getAttribute("loginType");
        //获取stpLogic
        StpLogic stpLogic = LoginType.getStpLogic(loginType);
        //用户登录
        stpLogic.login(loginUser.getUsername(), device);
        //获取token
        SaTokenInfo tokenInfo = stpLogic.getTokenInfo();
        if (WebHttpUtils.isAjaxRequest(request)) {
            java.io.PrintWriter writer = null;
            try {
                writer = response.getWriter();
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(writer, tokenInfo);
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
        } else {
            String tokenString = objectMapper.writeValueAsString(tokenInfo);
            String redirectUrl = WebHttpUtils.getRedirectParameter(request, tokenString);
            if (StringUtils.isNotBlank(redirectUrl)) {
                log.debug("redirectUrl: {}", redirectUrl);
                WebHttpUtils.sendRedirect(request, response, redirectUrl);
            } else {
                WebHttpUtils.sendRedirect(request, response, "/");
            }

        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher  applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
