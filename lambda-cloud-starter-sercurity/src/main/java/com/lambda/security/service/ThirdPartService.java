package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;

public interface ThirdPartService extends UserDetailService {

    LoginUser loadByOpenId(String openId, String loginType);

}