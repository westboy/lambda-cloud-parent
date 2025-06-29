package com.lambda.security.web.form;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.exception.*;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * DefaultAuthenticationProcessingFilter
 *
 * @author jpjoo
 */
@Setter
@Getter
public class FormAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private FormLockingStrategy formLockingStrategy;
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
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
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
            Map<String, Object> user = getUserLoginForRequestBody(request);
            if (MapUtils.isNotEmpty(user)) {
                username = (String) user.getOrDefault(this.getUsernameParameter(), "");
                password = (String) user.getOrDefault(this.getPasswordParameter(), "");
                if (StringUtils.isBlank(loginType)) {
                    loginType = (String) user.getOrDefault(Constants.LOGIN_TYPE, StpUtil.getLoginType());
                }
                if (StringUtils.isBlank(device)) {
                    device = (String) user.getOrDefault(Constants.LOGIN_DEVICE, "default");
                }
            }
        }
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("账号不能为空！");
        }
        if (StringUtils.isBlank(password)) {
            throw new UsernameNotFoundException("密码不能为空！");
        }
        if (formLockingStrategy.checkFailureTimes(username)) {
            throw new AccountLockedException("账号" + username + "已经被锁定:" + formLockingStrategy.getDuration()
                    + formLockingStrategy.getTimeUnit().name());
        }

        if (StrUtil.isEmpty(loginType)) {
            loginType = StpUtil.getLoginType();
        }

        boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);

        if (!containsLoginType) {
            throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
        }

        request.setAttribute(Constants.LOGIN_TYPE, loginType);

        if (StrUtil.isEmpty(device)) {
            device = "default";
        }
        request.setAttribute(Constants.LOGIN_DEVICE, device);

        LoginUser loginUser = userDetailService.loginByUsername(username, loginType);
        if (loginUser == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }

        if (loginUser.getAccountExpired() == null) {
            throw new VerifyCodeValidationException("the account is expired");
        }

        if (loginUser.getAccountLocked() == null) {
            throw new VerifyCodeValidationException("the account is locked");
        }

        String credentials = loginUser.getCredentials();
        boolean matches = passwordEncoder.matches(password, credentials);
        if (!matches) {
            formLockingStrategy.loginFailure(username);
            throw new AuthenticationException("密码错误！");
        }
        formLockingStrategy.loginSuccess(username);
        return loginUser;
    }

    private String obtainDevice(HttpServletRequest request) {
        return request.getParameter(Constants.LOGIN_DEVICE);
    }

    @Nullable
    protected String obtainLoginType(HttpServletRequest request) {
        return request.getParameter(Constants.LOGIN_TYPE);
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }
}
