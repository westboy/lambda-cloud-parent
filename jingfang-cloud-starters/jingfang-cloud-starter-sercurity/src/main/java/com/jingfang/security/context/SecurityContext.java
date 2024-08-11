package com.jingfang.security.context;

import com.jingfang.cloud.core.principal.LoginUser;

import java.io.Serializable;

/**
 * SecurityContext
 *
 * @author jpjoo
 */
public interface SecurityContext extends Serializable {


    /**
     * 获取用户
     *
     * @return LoginUser
     */
    LoginUser getPrincipal();

    /**
     * 设置用户 loginUser
     *
     * @param loginUser
     */
    void setPrincipal(LoginUser loginUser);
}