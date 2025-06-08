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
import java.util.Optional;

/**
 * ThirdPartAuthenticationProcessingFilter
 *
 * @author Jin
 */
public class ThirdPartAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final SecurityProperties.ThirdPartLogin thirdPartLogin;
    private final List<ThirdPartLoginProvider> thirdPartLoginProviders;

    public ThirdPartAuthenticationProcessingFilter(SecurityProperties.ThirdPartLogin thirdPartLogin, List<ThirdPartLoginProvider> thirdPartLoginProviders) {
        super(thirdPartLogin.getLoginPath());
        this.thirdPartLogin = thirdPartLogin;
        this.thirdPartLoginProviders = thirdPartLoginProviders;
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Map<String, Object> thirdLogin = getUserLoginForRequestBody(request);

        this.validateRequest(thirdLogin, thirdPartLogin);

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        String code = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthCode(), "");
        String loginType = (String) thirdLogin.getOrDefault("loginType", "admin");

        ThirdPartLoginProvider loginProvider = getThirdPartLoginProvider(thirdId)
                .orElseThrow(() -> new AuthenticationException(thirdPartLogin.getThirdName() + " is empty"));

        return loginProvider.authenticate(code, loginType);
    }

    private void validateRequest(Map<String, Object> thirdLogin, SecurityProperties.ThirdPartLogin thirdPartLogin) {
        if (MapUtils.isEmpty(thirdLogin)) {
            throw new AuthenticationException("Request body is empty");
        }

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        if (StrUtil.isBlank(thirdId)) {
            throw new AuthenticationException("thirdName is empty");
        }

        String code = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthCode(), "");
        if (StrUtil.isBlank(code)) {
            throw new AuthenticationException("thirdAuthCode is empty");
        }
    }

    private Optional<ThirdPartLoginProvider> getThirdPartLoginProvider(String thirdId) {
        return thirdPartLoginProviders.stream()
                .filter(provider -> provider.support(thirdId))
                .findFirst();
    }

}
