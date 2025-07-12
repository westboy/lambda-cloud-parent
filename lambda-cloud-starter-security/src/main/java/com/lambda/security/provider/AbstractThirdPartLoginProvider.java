package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.provider.model.ThirdLoginResult;
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
        ThirdLoginResult thirdLoginResultParam = getThirdLoginParam(token);
        return thirdPartyLoginService.loadByThirdLoginResult(thirdLoginResultParam, loginType);
    }

    public abstract ThirdLoginResult getThirdLoginParam(String code);

    public String buildAuthorizationUrl(String state, String scope, String redirectUri) {
        throw new UnsupportedOperationException("Not support buildAuthorizationUrl");
    }
}
