package com.lambda.security.web.third;

import cn.hutool.core.util.StrUtil;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.provider.ThirdPartLoginProvider;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class ThirdPartAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private SecurityProperties securityProperties;

    private List<ThirdPartLoginProvider> thirdPartLoginProviders;

    protected ThirdPartAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        SecurityProperties.ThirdPartLogin thirdPartLogin = this.securityProperties.getThirdPartLogin();

        Map<String, Object> thirdLogin = getUserLoginForRequestBody(request);
        if (MapUtils.isEmpty(thirdLogin)) {
            throw new AuthenticationException("Request body is empty");
        }

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdId(), "");

        if (StrUtil.isBlank(thirdId)) {
            throw new AuthenticationException("thirdId is empty");
        }

        String code = (String) thirdLogin.getOrDefault(thirdPartLogin.getCode(), "");

        if (StrUtil.isBlank(code)) {
            throw new AuthenticationException("code is empty");
        }

        String loginType = (String) thirdLogin.getOrDefault("loginType", "");

        ThirdPartLoginProvider loginProvider = getThirdPartLoginProvider(thirdPartLogin.getThirdId());


        LoginUser loginUser = loginProvider.authenticate(code, loginType);

        return loginUser;
    }

    private ThirdPartLoginProvider getThirdPartLoginProvider(String thirdId) {
        for (ThirdPartLoginProvider thirdPartLoginProvider : thirdPartLoginProviders) {
            boolean support = thirdPartLoginProvider.support(thirdId);
            if (support) {
                return thirdPartLoginProvider;
            }
        }
        return null;
    }

}
