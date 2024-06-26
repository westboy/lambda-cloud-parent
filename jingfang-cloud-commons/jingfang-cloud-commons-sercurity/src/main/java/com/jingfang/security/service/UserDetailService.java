package com.jingfang.security.service;

import cn.dev33.satoken.stp.StpInterface;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.exception.AuthenticationException;

/**
 * UserDetailService
 *
 * @author jin
 */
public interface UserDetailService extends StpInterface {


    /**
     * 用户登录
     *
     * @param username
     * @param loginType
     * @return
     * @throws AuthenticationException
     */
    LoginUser loginByUsername(String username, String loginType) throws AuthenticationException;

}
