package com.lambda.security.inteceptor;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * SecureInterceptor
 *
 * @author jin
 */
@Slf4j
public class SecureInterceptor implements SaParamFunction<Object> {

    private final SecureExtendInterceptor secureExtendInterceptor;

    public SecureInterceptor(SecureExtendInterceptor secureExtendInterceptor) {
        this.secureExtendInterceptor = secureExtendInterceptor;
    }

    @Override
    public void run(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.getBeanType().isAssignableFrom(BasicErrorController.class)) {
                return;
            }
        }
        if (handler instanceof ResourceHttpRequestHandler) {
            return;
        }

        StpLogic stpLogic = LoginType.getActiveStpLogic();

        stpLogic.checkLogin();

        if (secureExtendInterceptor != null) {
            secureExtendInterceptor.handle(handler, stpLogic);
        }
    }
}
