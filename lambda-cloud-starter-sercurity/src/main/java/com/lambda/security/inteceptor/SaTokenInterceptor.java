package com.lambda.security.inteceptor;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.cloud.core.utils.OperatorUtils;
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
public class SaTokenInterceptor implements SaParamFunction<Object> {

    private final SecureInterceptor secureInterceptor;

    public SaTokenInterceptor(SecureInterceptor secureInterceptor) {
        this.secureInterceptor = secureInterceptor;
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

        if (secureInterceptor != null) {
            secureInterceptor.handle(handler, stpLogic,OperatorUtils.getOperator());
        }
    }
}
