package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;

public interface ThirdPartLoginProvider {

    LoginUser authenticate(String token,String loginType);

    boolean support(String thirdId);
}
