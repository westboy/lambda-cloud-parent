package com.lambda.security.inteceptor;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.utils.OperatorUtils;
import com.lambda.cloud.core.utils.StpLogicUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Sa-Token拦截器
 * <p>
 * 该类实现了{@link SaParamFunction}接口，作为Sa-Token框架的拦截器适配器，
 * 将Sa-Token的拦截逻辑与自定义的安全拦截器进行桥接。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>拦截器适配：</strong>将Sa-Token拦截器与自定义安全拦截器进行适配</li>
 *   <li><strong>处理器过滤：</strong>过滤不需要安全检查的处理器（如错误控制器、静态资源）</li>
 *   <li><strong>用户上下文传递：</strong>获取当前登录用户并传递给安全拦截器</li>
 *   <li><strong>StpLogic集成：</strong>与Sa-Token的StpLogic进行深度集成</li>
 * </ul>
 *
 * <h3>设计模式：</h3>
 * <ul>
 *   <li><strong>适配器模式：</strong>适配Sa-Token框架与自定义安全逻辑</li>
 *   <li><strong>委托模式：</strong>将具体的安全处理委托给SecureInterceptor</li>
 *   <li><strong>记录类模式：</strong>使用Java 14+的record语法简化代码</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>接收Sa-Token框架的拦截回调</li>
 *   <li>检查处理器类型，过滤不需要处理的请求</li>
 *   <li>获取当前活跃的StpLogic实例</li>
 *   <li>提取当前登录用户信息</li>
 *   <li>委托给SecureInterceptor进行具体的安全处理</li>
 * </ol>
 *
 * <h3>过滤策略：</h3>
 * <ul>
 *   <li><strong>静态资源：</strong>跳过{@link ResourceHttpRequestHandler}的处理</li>
 *   <li><strong>业务处理器：</strong>只处理{@link HandlerMethod}类型的业务处理器</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 创建安全拦截器
 * SecureInterceptor secureInterceptor = new SecureInterceptor();
 *
 * // 创建Sa-Token拦截器适配器
 * SaTokenInterceptor saTokenInterceptor = new SaTokenInterceptor(secureInterceptor);
 *
 * // 在Sa-Token配置中注册
 * @Configuration
 * public class SaTokenConfig {
 *     @Bean
 *     public SaServletFilter getSaServletFilter() {
 *         return new SaServletFilter()
 *             .addInclude("/**")
 *             .addExclude("/favicon.ico")
 *             .setAuth(saTokenInterceptor);
 *     }
 * }
 * }</pre>
 *
 * <h3>技术特点：</h3>
 * <ul>
 *   <li><strong>Record类型：</strong>使用Java 14+的record语法，代码简洁</li>
 *   <li><strong>函数式接口：</strong>实现Sa-Token的函数式拦截接口</li>
 *   <li><strong>类型安全：</strong>通过泛型和类型检查确保安全性</li>
 *   <li><strong>性能优化：</strong>提前过滤不需要处理的请求类型</li>
 * </ul>
 *
 * @param secureInterceptor 自定义安全拦截器，负责具体的安全处理逻辑
 * @author jin
 * @see SecureInterceptor
 * @see SaParamFunction
 * @see StpLogic
 * @see OperatorUtils
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
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>类型检查：</strong>检查处理器类型，过滤特殊处理器</li>
     *   <li><strong>错误控制器过滤：</strong>跳过Spring Boot的错误处理控制器</li>
     *   <li><strong>静态资源过滤：</strong>跳过静态资源请求处理器</li>
     *   <li><strong>获取安全上下文：</strong>获取当前的StpLogic实例</li>
     *   <li><strong>提取用户信息：</strong>从StpLogic中提取当前登录用户</li>
     *   <li><strong>委托处理：</strong>将安全处理委托给SecureInterceptor</li>
     * </ol>
     *
     * <h3>过滤规则：</h3>
     * <ul>
     *   <li><strong>BasicErrorController：</strong>Spring Boot的错误处理控制器，直接跳过</li>
     *   <li><strong>ResourceHttpRequestHandler：</strong>静态资源处理器，直接跳过</li>
     *   <li><strong>其他HandlerMethod：</strong>业务处理器，进行安全拦截</li>
     * </ul>
     *
     * <h3>安全上下文：</h3>
     * <ul>
     *   <li>通过{@link StpLogicUtils#getActiveStpLogic()}获取当前StpLogic</li>
     *   <li>通过{@link OperatorUtils#getLoginUser(StpLogic)}获取登录用户</li>
     *   <li>将完整的安全上下文传递给 SecureInterceptor</li>
     * </ul>
     *
     * @param handler 请求处理器对象，可能是HandlerMethod或ResourceHttpRequestHandler等
     */
    @Override
    public void run(Object handler) {
        // ========== 处理器类型过滤 ==========

        // 检查是否为业务处理方法
        if (handler instanceof HandlerMethod handlerMethod) {
            // 跳过Spring Boot错误控制器的处理
            if (handlerMethod.getBeanType().isAssignableFrom(BasicErrorController.class)) {
                return;
            }
        }

        // 跳过静态资源请求处理器
        if (handler instanceof ResourceHttpRequestHandler) {
            return;
        }

        // ========== 安全拦截处理 ==========

        // 获取当前活跃的 StpLogic 实例
        StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();

        // 委托给 SecureInterceptor 进行具体的安全处理
        if (secureInterceptor != null) {
            secureInterceptor.handle(handler, stpLogic, OperatorUtils.getLoginUser(stpLogic));
        }
    }
}
