package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;

public interface ThirdPartyLoginService {

    LoginUser loadByOpenId(String openId, String loginType);
}
