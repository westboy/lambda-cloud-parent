package com.lambda.security.web.third;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.BadCredentialsException;
import com.lambda.security.provider.ThirdPartLoginProvider;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;

/**
 * ThirdPartAuthenticationProcessingFilter
 *
 * @author Jin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class ThirdPartAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final SecurityProperties.ThirdPartLogin thirdPartLogin;
    private final List<ThirdPartLoginProvider> thirdPartLoginProviders;

    public ThirdPartAuthenticationProcessingFilter(
            SecurityProperties.ThirdPartLogin thirdPartLogin, List<ThirdPartLoginProvider> thirdPartLoginProviders) {
        super(thirdPartLogin.getLoginPath());
        this.thirdPartLogin = thirdPartLogin;
        this.thirdPartLoginProviders = thirdPartLoginProviders;
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        Map<String, Object> thirdLogin = getUserLoginForRequestBody(request);

        this.validateRequest(thirdLogin, thirdPartLogin);

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        String authParam = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthParam(), "");
        String loginType = (String) thirdLogin.getOrDefault("loginType", StpUtil.getLoginType());

        boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);
        if (!containsLoginType) {
            throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
        }

        ThirdPartLoginProvider loginProvider = getThirdPartLoginProvider(thirdId)
                .orElseThrow(() -> new AuthenticationException(thirdPartLogin.getThirdName() + " is empty"));

        return loginProvider.authenticate(authParam, loginType);
    }

    private void validateRequest(Map<String, Object> thirdLogin, SecurityProperties.ThirdPartLogin thirdPartLogin) {
        if (MapUtils.isEmpty(thirdLogin)) {
            throw new BadCredentialsException("Request body is empty");
        }

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        if (StrUtil.isBlank(thirdId)) {
            throw new BadCredentialsException("thirdName is empty");
        }

        String code = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthParam(), "");
        if (StrUtil.isBlank(code)) {
            throw new BadCredentialsException("thirdAuthCode is empty");
        }
    }

    private Optional<ThirdPartLoginProvider> getThirdPartLoginProvider(String thirdId) {
        return thirdPartLoginProviders.stream()
                .filter(provider -> provider.support(thirdId))
                .findFirst();
    }
}
