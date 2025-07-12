package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.service.ThirdPartyLoginService;
import lombok.RequiredArgsConstructor;

/**
 * AbstractThirdPartLoginProvider
 *
 * @author Jin
 */
@RequiredArgsConstructor
public abstract class AbstractThirdPartLoginProvider implements ThirdPartLoginProvider {

    private final ThirdPartyLoginService thirdPartyLoginService;

    @Override
    public LoginUser authenticate(String token, String loginType) {
        ThirdPartLoginResult thirdPartLoginResult = getThirdLoginParam(token);
        return thirdPartyLoginService.loadByThirdLoginResult(thirdPartLoginResult, loginType);
    }

    public abstract ThirdPartLoginResult getThirdLoginParam(String code);

    public String buildAuthorizationUrl(String state, String scope, String redirectUri) {
        throw new UnsupportedOperationException("Not support buildAuthorizationUrl");
    }
}
