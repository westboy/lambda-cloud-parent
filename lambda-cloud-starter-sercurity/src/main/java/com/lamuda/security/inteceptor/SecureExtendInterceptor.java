package com.lambda.security.inteceptor;

import cn.dev33.satoken.stp.StpLogic;

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
    void handle(Object handler, StpLogic stpLogic);
}
