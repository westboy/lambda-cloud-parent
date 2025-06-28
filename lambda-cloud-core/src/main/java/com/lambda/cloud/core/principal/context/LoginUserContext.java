package com.lambda.cloud.core.principal.context;

import com.lambda.cloud.core.principal.LoginUser;
import java.io.Serializable;

/**
 * SecurityContext
 *
 * @author jpjoo
 */
public interface LoginUserContext extends Serializable {

    /**
     * 获取用户
     *
     * @return LoginUser
     */
    LoginUser getPrincipal();

    /**
     * 设置用户 loginUser
     *
     */
    void setPrincipal(LoginUser loginUser);
}
