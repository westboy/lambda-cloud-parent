package com.lambda.security.web.form;

import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import com.lambda.security.web.SecurityLockingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * DefaultAuthenticationProcessingFilter
 *
 * @author jpjoo
 */
@Setter
@Getter
public class FormAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    public static final String LOGIN_FORM_PARAMETERS = "loginFormParameters";
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private String loginTypeParameter = "loginType";
    private String deviceParameter = "loginDevice";
    private SecurityLockingStrategy securityLockingStrategy;
    private UserDetailService userDetailService;
    private PasswordEncoder passwordEncoder;

    public FormAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    public void setUserDetailService(UserDetailService userDetailService) {
        Assert.notNull(userDetailService, "userDetailService is null, please impl userDetailService");
        this.userDetailService = userDetailService;
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationException("Authentication method not supported: " + request.getMethod());
        }
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String device = obtainDevice(request);
        String loginType = obtainLoginType(request);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        if (device == null) {
            device = "";
        }

        if (loginType == null) {
            loginType = "";
        }

        username = username.trim();

        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            Map<String, Object> user = getUserFromRequestBody(request);
            if (MapUtils.isNotEmpty(user)) {
                username = (String) user.getOrDefault(this.getUsernameParameter(), "");
                password = (String) user.getOrDefault(this.getPasswordParameter(), "");
                if (StringUtils.isBlank(loginType)) {
                    loginType = (String) user.getOrDefault(this.getLoginTypeParameter(), "admin");
                }
                if (StringUtils.isBlank(device)) {
                    device = (String) user.getOrDefault(this.getDeviceParameter(), "default");
                }
            }
        }
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationException("账号不能为空！");
        }
        if (StringUtils.isBlank(password)) {
            throw new AuthenticationException("密码不能为空！");
        }
        if (securityLockingStrategy.checkFailureTimes(username)) {
            throw new AuthenticationException("账号" + username + "已经被锁定:" + securityLockingStrategy.getDuration() + securityLockingStrategy.getTimeUnit().name());
        }

        if (StrUtil.isEmpty(loginType)) {
            loginType = LoginType.ADMIN.getCode();
        }

        request.setAttribute(loginTypeParameter, loginType);

        if (StrUtil.isEmpty(device)) {
            device = "default";
        }
        request.setAttribute(deviceParameter, device);


        LoginUser loginUser = userDetailService.loginByUsername(username, loginType);
        if (loginUser == null) {
            throw new AuthenticationException("用户不存在！");
        }
        String credentials = loginUser.getCredentials();
        boolean matches = passwordEncoder.matches(password, credentials);
        if (!matches) {
            securityLockingStrategy.loginFailure(username);
            throw new AuthenticationException("密码错误！");
        }
        securityLockingStrategy.loginSuccess(username);
        return loginUser;
    }

    private String obtainDevice(HttpServletRequest request) {
        return request.getParameter(this.deviceParameter);
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
