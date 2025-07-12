package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.provider.model.ThirdLoginResult;

public interface ThirdPartyLoginService {

    LoginUser loadByThirdLoginResult(ThirdLoginResult thirdLoginResult, String loginType);
}
