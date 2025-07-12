package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;

/**
 * ThirdPartLoginProvider
 *
 * @author Jin
 */
public interface ThirdPartLoginProvider {

    LoginUser authenticate(String token, String loginType);

    boolean support(String thirdType);

    String getThirdType();
}
