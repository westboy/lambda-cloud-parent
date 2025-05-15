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
import org.apache.commons.lang.StringUtils;
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

        String mobile = obtainMobileParameter(request);
        String device = obtainDeviceParameter(request);
        String loginType = obtainLoginTypeParameter(request);

        if (mobile == null) {
            mobile = "";
        }

        if (device == null) {
            device = "";
        }

        if (loginType == null) {
            loginType = "";
        }

        if (StringUtils.isBlank(mobile)) {
            Map<String, Object> smsLogin = getUserLoginForRequestBody(request);
            if (MapUtils.isNotEmpty(smsLogin)) {
                mobile = (String) smsLogin.getOrDefault(this.userDetailService, "");
                if (StringUtils.isBlank(loginType)) {
                    loginType = (String) smsLogin.getOrDefault(this.loginTypeParameter, "admin");
                }
                if (StringUtils.isBlank(device)) {
                    device = (String) smsLogin.getOrDefault(this.deviceParameter, "default");
                }
            }
        }

        if (StrUtil.isEmpty(loginType)) {
            loginType = LoginType.ADMIN.getCode();
        }

        request.setAttribute(loginTypeParameter, loginType);

        if (StrUtil.isEmpty(device)) {
            device = "default";
        }
        request.setAttribute(deviceParameter, device);

        LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

        if (loginUser == null) {
            throw new AuthenticationException("用户不存在！");
        }

        return loginUser;
    }

    public String obtainMobileParameter(HttpServletRequest request) {
        return request.getParameter(mobileParameter);
    }

    public String obtainLoginTypeParameter(HttpServletRequest request) {
        return request.getParameter(loginTypeParameter);
    }

    public String obtainDeviceParameter(HttpServletRequest request) {
        return request.getParameter(deviceParameter);
    }
}
