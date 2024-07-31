package com.jingfang.security.inteceptor;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextHolder;
import com.jingfang.security.context.SecurityContextImpl;
import com.jingfang.security.enums.LoginType;
import com.jingfang.security.service.UserDetailService;
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

    private final UserDetailService userDetailService;
    private final SecureExtendInterceptor secureExtendInterceptor;

    public SecureInterceptor(UserDetailService userDetailService, SecureExtendInterceptor secureExtendInterceptor) {
        this.userDetailService = userDetailService;
        this.secureExtendInterceptor = secureExtendInterceptor;
    }

    @Override
    public void run(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getBeanType().isAssignableFrom(BasicErrorController.class)) {
                return;
            }
        }
        if (handler instanceof ResourceHttpRequestHandler) {
            return;
        }
        StpLogic stpLogic = LoginType.ADMIN.getStpLogic().isLogin() ? LoginType.ADMIN.getStpLogic() : LoginType.USER.getStpLogic();
        stpLogic.checkLogin();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            try {
                LoginUser loginUser = userDetailService.loginByUsername((String) stpLogic.getLoginId(), stpLogic.getLoginType());
                SecurityContextHolder.setContext(new SecurityContextImpl(loginUser));
                log.info("reload principal context");
            } catch (Exception exception) {
                stpLogic.logout();
                log.info("reload principal context fail", exception);
            }
        }
        if (secureExtendInterceptor != null) {
            secureExtendInterceptor.handle(handler, stpLogic);
        }
    }
}
