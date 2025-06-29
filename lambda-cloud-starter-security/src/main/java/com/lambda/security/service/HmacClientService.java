package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;

/**
 * HmacClientService
 *
 * @author jpjoo
 */
public interface HmacClientService {

    /**
     * loadClientByAppid
     *
     * @param appid
     * @return
     */
    LoginUser loadClientByAppid(String appid);

    /**
     * loginByUsername
     *
     * @param username
     * @param loginType
     * @return
     * @throws AuthenticationException
     */
    LoginUser loginByUsername(String username, String loginType) throws AuthenticationException;
}
