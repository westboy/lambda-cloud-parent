package com.lambda.security.handler.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.RequestTimeHolder;
import com.lambda.security.events.UserLoginEvent;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

/**
 * 默认认证成功处理器
 *
 * @author jpjoo
 */
@Slf4j
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@SuppressWarnings("all")
public class CommonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public CommonAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException {
        // sa-token 登录
        // 登录设备
        String device = (String) request.getAttribute("loginDevice");
        // 多用户类型支持
        String loginType = (String) request.getAttribute("loginType");
        // 获取stpLogic
        StpLogic stpLogic = LoginType.getStpLogic(loginType);
        // 用户登录
        stpLogic.login(loginUser.getName(), device);
        // 存储用户信息
        stpLogic.getTokenSession().set("loginUser", loginUser);
        // 获取token
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
        // 获取用户登录时的IP,需要Nginx做相关配置防止IP伪造
        String remoteAddr = JakartaServletUtil.getClientIP(request);
        // 获取用户登录时的端口
        int remotePort = request.getRemotePort();
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        // 发布登录事件
        SpringUtil.publishEvent(new UserLoginEvent(loginUser, cast, remoteAddr, remotePort));
    }
}
