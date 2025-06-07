package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.service.ThirdPartService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractThirdPartLoginProvider implements ThirdPartLoginProvider {

    private final ThirdPartService thirdPartService;

    @Override
    public LoginUser authenticate(String token, String loginType) {
        return thirdPartService.loadByOpenId(token, loginType);
    }

    public abstract String getOpenId(String code);
}
