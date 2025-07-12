package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.provider.ThirdPartLoginResult;

public interface ThirdPartyLoginService {

    LoginUser loadByThirdLoginResult(ThirdPartLoginResult thirdLoginResult, String loginType);
}
