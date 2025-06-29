package com.lambda.security.inteceptor;

import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginUser;

/**
 * SaTokenCheckHandler
 *
 * @author jpjoo
 */
public interface SecureInterceptor {

    /**
     * handle
     *
     * @param handler  Object
     * @param stpLogic StpLogic
     * @param operator LoginUser
     */
    void handle(Object handler, StpLogic stpLogic, LoginUser operator);
}
