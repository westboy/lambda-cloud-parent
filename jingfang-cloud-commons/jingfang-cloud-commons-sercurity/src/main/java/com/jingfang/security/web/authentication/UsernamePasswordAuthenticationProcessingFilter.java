package com.jingfang.security.web.authentication;

import cn.hutool.extra.servlet.ServletUtil;
import com.jingfang.cloud.core.principal.Principal;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.cloud.web.RequestTimeHolder;
import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.password.StandardPasswordEncoder;
import com.jingfang.security.service.UserDetailService;
import com.jingfang.security.web.AbstractAuthenticationProcessingFilter;
import com.jingfang.security.web.authentication.lock.SecurityLockingStrategy;
import com.jingfang.security.web.events.UserLoginEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Setter
@Getter
public class UsernamePasswordAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    public static final String LOGIN_FORM_PARAMETERS = "loginFormParameters";
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private String loginTypeParameter = "loginType";
    private SecurityLockingStrategy securityLockingStrategy;
    private UserDetailService userDetailService;
    private StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();
    private ApplicationEventPublisher applicationEventPublisher;
    protected UsernamePasswordAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Principal attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationException("Authentication method not supported: " + request.getMethod());
        }
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String loginType = obtainLoginType(request);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            Map<String, Object> user = getUserFromRequestBody(request);
            if (MapUtils.isNotEmpty(user)) {
                username = (String) user.getOrDefault(this.getUsernameParameter(), "");
                password = (String) user.getOrDefault(this.getPasswordParameter(), "");
                loginType = (String) user.getOrDefault(this.getLoginTypeParameter(), "");
            }
        }
        if (StringUtils.isBlank(username)) {
            ;
        }
        if (securityLockingStrategy.checkFailureTimes(username)) {
            throw new AuthenticationException("账号已经被锁定： " + username);
        }
        Principal principal = userDetailService.loginByUsername(username,loginType);
        if (principal == null) {
            throw new AuthenticationException("用户不存在！");
        }
        String credentials = principal.getCredentials();
        boolean matches = standardPasswordEncoder.matches(password, credentials);
        if (!matches) {
            throw new AuthenticationException("密码错误！");
        }
        //获取用户登录时的IP,需要Nginx做相关配置防止IP伪造
        String remoteAddr = ServletUtil.getClientIP(request);
        //获取用户登录时的端口
        int remotePort = request.getRemotePort();
        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        //发布登录时间
        applicationEventPublisher.publishEvent(new UserLoginEvent(principal,cast,remoteAddr,remotePort));
        return principal;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Nullable
    protected String obtainLoginType(HttpServletRequest request) {
        return request.getParameter(this.loginTypeParameter);
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }
    public Map<String, Object> getUserFromRequestBody(HttpServletRequest request) {
        try {
            Map<String, Object> loginParameters = WebHttpUtils.getRequestBody(request);
            request.setAttribute(LOGIN_FORM_PARAMETERS, loginParameters);
            return loginParameters;
        } catch (Exception e) {
            throw new AuthenticationException("获取用户数据失败！");
        }
    }

}
