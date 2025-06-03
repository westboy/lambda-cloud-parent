package com.lambda.security.inteceptor;

import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginUser;

/**
 * SaTokenCheckHandler
 *
 * @author jpjoo
 */
public interface SecureExtendInterceptor {

    /**
     * handle
     *
     * @param handler
     * @param stpLogic
     */
    void handle(Object handler, StpLogic stpLogic, LoginUser operator);
}
