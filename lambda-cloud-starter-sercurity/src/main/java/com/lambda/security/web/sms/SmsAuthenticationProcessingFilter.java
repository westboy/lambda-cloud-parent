package com.lambda.security.web.sms;


import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;

/**
 * SmsAuthenticationProcessingFilter
 *
 * @author Jin
 **/
@SuppressWarnings("all")
public class SmsAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private String codeParameter = "code";
    private String mobileParameter = "mobile";
    private String loginTypeParameter = "loginType";
    private String deviceParameter = "loginDevice";

    private UserDetailService userDetailService;

    public SmsAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationException("Authentication method not supported: " + request.getMethod());
        }

        Map<String, Object> smsLogin = getUserLoginForRequestBody(request);
        if (MapUtils.isEmpty(smsLogin)) {
            throw new AuthenticationException("Request body is empty");
        }
        String mobile = (String) smsLogin.getOrDefault(this.mobileParameter, "");

        if (StrUtil.isBlank(mobile)) {
            throw new AuthenticationException("Mobile is empty");
        }

        String loginType = (String) smsLogin.getOrDefault(this.loginTypeParameter, LoginType.ADMIN.getCode());
        request.setAttribute(loginTypeParameter, loginType);

        String device = (String) smsLogin.getOrDefault(this.deviceParameter, "default");
        request.setAttribute(deviceParameter, device);

        LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

        if (loginUser == null) {
            throw new AuthenticationException("User not found");
        }

        return loginUser;
    }
}
