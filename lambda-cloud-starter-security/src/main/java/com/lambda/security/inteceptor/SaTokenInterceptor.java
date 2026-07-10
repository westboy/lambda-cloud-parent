package com.lambda.security.inteceptor;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.lambda.cloud.core.utils.OperatorUtils;
import com.lambda.cloud.core.utils.StpLogicUtils;
import jakarta.servlet.DispatcherType;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Sa-Token拦截器
 * <p>
 * 该类实现了{@link SaParamFunction}接口，作为Sa-Token框架的拦截器适配器，
 * 将Sa-Token的拦截逻辑与自定义的安全拦截器进行桥接。
 * </p>
 */
@Slf4j
public record SaTokenInterceptor(SecureInterceptor secureInterceptor) implements SaParamFunction<Object> {

    /**
     * 执行拦截处理逻辑
     * <p>
     * 该方法是Sa-Token框架的回调入口，负责对请求进行安全拦截处理。
     * 会根据处理器类型进行过滤，只对需要安全检查的业务处理器进行拦截。
     * </p>
     *
     * @param handler 请求处理器对象，可能是HandlerMethod或ResourceHttpRequestHandler等
     */
    @Override
    public void run(Object handler) {

        // SSE 等异步端点在“完成”时会触发 ASYNC 分发回到 DispatcherServlet，此时
        // Sa-Token 的 ThreadLocal 上下文不会被过滤器初始化，继续鉴权会抛
        // SaTokenContextException(“上下文尚未初始化”)。初始 REQUEST 分发已做过
        // 鉴权，ASYNC 分发只是用来收尾，直接放行即可。
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes
                && servletRequestAttributes.getRequest().getDispatcherType() == DispatcherType.ASYNC) {
            return;
        }

        // 跳过静态资源请求处理器
        if (handler instanceof ResourceHttpRequestHandler) {
            log.debug("Skipping handler: {}", handler);
            return;
        }

        Method method = null;
        // 检查是否为业务处理方法
        if (handler instanceof HandlerMethod handlerMethod) {
            // 跳过Spring Boot错误控制器的处理
            if (handlerMethod.getBeanType().isAssignableFrom(BasicErrorController.class)) {
                log.debug("Skipping handler: {}", handler);
                return;
            }
            method = handlerMethod.getMethod();
        }

        // ========== 安全拦截处理 ==========

        // 获取当前活跃的 StpLogic 实例
        StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();

        if (stpLogic.hasRole("ROLE_DEV")) {
            SaRouter.stop();
        }

        if (method != null) {
            SaAnnotationStrategy.instance.checkMethodAnnotation.accept(method);
        }

        // 委托给 SecureInterceptor 进行具体的安全处理
        if (secureInterceptor != null) {
            secureInterceptor.handle(handler, stpLogic, OperatorUtils.getLoginUser(stpLogic));
        }
    }
}
