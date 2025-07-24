package com.lambda.security.web.form;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.OperatorUtils;
import com.lambda.security.handler.LogoutHandler;
import com.lambda.security.handler.LogoutSuccessHandler;
import com.lambda.security.handler.impl.CompositeLogoutHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.log.LogMessage;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 表单登出过滤器
 * <p>
 * 该过滤器负责处理用户登出请求，提供完整的登出流程管理。
 * 当用户访问登出URL时，过滤器会执行登出处理逻辑，清理用户会话，
 * 并调用登出成功处理器完成后续操作。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>统一登出</strong>：提供标准化的登出处理流程</li>
 *   <li><strong>灵活配置</strong>：支持自定义登出URL和处理器</li>
 *   <li><strong>组合处理</strong>：支持多个登出处理器的组合使用</li>
 *   <li><strong>安全清理</strong>：确保用户会话和相关数据的完全清理</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>URL匹配：检查请求是否匹配登出URL模式</li>
 *   <li>用户识别：获取当前登录用户信息</li>
 *   <li>登出处理：执行登出逻辑，清理会话数据</li>
 *   <li>成功回调：调用登出成功处理器</li>
 *   <li>日志记录：记录登出过程的详细信息</li>
 * </ul>
 *
 * <h3>登出流程：</h3>
 * <ol>
 *   <li>检查请求URL是否匹配登出路径</li>
 *   <li>获取当前登录用户信息</li>
 *   <li>调用登出处理器执行清理操作</li>
 *   <li>调用登出成功处理器</li>
 *   <li>记录登出日志</li>
 * </ol>
 *
 * <h3>配置示例：</h3>
 * <pre>{@code
 * @Bean
 * public FormLogoutFilter logoutFilter() {
 *     return new FormLogoutFilter(
 *         "/api/logout",
 *         logoutSuccessHandler,
 *         sessionLogoutHandler,
 *         tokenLogoutHandler
 *     );
 * }
 * }</pre>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>用户主动登出</li>
 *   <li>会话超时清理</li>
 *   <li>安全登出处理</li>
 *   <li>多端登出同步</li>
 * </ul>
 *
 * @author jpjoo
 * @see LogoutHandler
 * @see LogoutSuccessHandler
 * @see CompositeLogoutHandler
 */
public class FormLogoutFilter extends GenericFilterBean {
    /**
     * Ant路径匹配器
     * <p>
     * 用于匹配登出请求的URL路径，支持Ant风格的路径模式，
     * 如通配符、路径变量等灵活的匹配规则。
     * </p>
     */
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 登出处理器
     * <p>
     * 负责执行实际的登出逻辑，如清理会话、删除令牌、
     * 清除缓存等操作。通常是CompositeLogoutHandler的实例，
     * 可以组合多个具体的登出处理器。
     * </p>
     *
     * @see CompositeLogoutHandler
     */
    private final LogoutHandler handler;

    /**
     * 登出成功处理器
     * <p>
     * 在登出操作完成后调用，负责处理登出成功的后续操作，
     * 如重定向到登录页面、返回成功响应、记录审计日志等。
     * </p>
     *
     * @see LogoutSuccessHandler
     */
    private final LogoutSuccessHandler logoutSuccessHandler;

    /**
     * 过滤器处理URL
     * <p>
     * 指定触发登出处理的URL路径。只有匹配此路径的请求
     * 才会被过滤器处理，其他请求将继续传递给过滤器链。
     * </p>
     *
     * <h3>常见配置：</h3>
     * <ul>
     *   <li>/logout - 标准登出路径</li>
     *   <li>/api/logout - API登出路径</li>
     *   <li>/admin/logout - 管理员登出路径</li>
     * </ul>
     */
    private final String filterProcessesUrl;

    /**
     * 构造表单登出过滤器（使用默认登出URL）
     * <p>
     * 创建一个使用默认登出URL（/logout）的表单登出过滤器实例。
     * 适用于标准的登出场景，无需自定义登出路径。
     * </p>
     *
     * @param logoutSuccessHandler 登出成功处理器，不能为null
     * @param handlers 登出处理器数组，可以传入多个处理器
     * @throws IllegalArgumentException 如果logoutSuccessHandler为null
     */
    public FormLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler... handlers) {
        this.handler = new CompositeLogoutHandler(handlers);
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler cannot be null");
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.filterProcessesUrl = "/logout";
    }

    /**
     * 构造表单登出过滤器（自定义登出URL）
     * <p>
     * 创建一个使用自定义登出URL的表单登出过滤器实例。
     * 适用于需要自定义登出路径的场景，如API接口、管理后台等。
     * </p>
     *
     * <h3>URL配置示例：</h3>
     * <ul>
     *   <li>/api/v1/logout - RESTful API登出</li>
     *   <li>/admin/logout - 管理员登出</li>
     *   <li>/mobile/logout - 移动端登出</li>
     * </ul>
     *
     * @param filterProcessesUrl 自定义的登出处理URL
     * @param logoutSuccessHandler 登出成功处理器，不能为null
     * @param handlers 登出处理器数组，可以传入多个处理器
     * @throws IllegalArgumentException 如果logoutSuccessHandler为null
     */
    public FormLogoutFilter(
            String filterProcessesUrl, LogoutSuccessHandler logoutSuccessHandler, LogoutHandler... handlers) {
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler cannot be null");
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.filterProcessesUrl = filterProcessesUrl;
        this.handler = new CompositeLogoutHandler(handlers);
    }

    /**
     * 执行过滤器处理
     * <p>
     * 过滤器的入口方法，将ServletRequest和ServletResponse转换为
     * HttpServletRequest和HttpServletResponse，然后调用具体的处理方法。
     * </p>
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param chain 过滤器链
     * @throws IOException 输入输出异常
     * @throws ServletException Servlet异常
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    /**
     * 执行HTTP登出过滤处理
     * <p>
     * 核心的登出处理逻辑，检查请求是否需要登出处理，
     * 如果需要则执行完整的登出流程，否则继续过滤器链。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>检查请求是否匹配登出URL</li>
     *   <li>获取当前登录用户信息</li>
     *   <li>记录登出调试日志</li>
     *   <li>调用登出处理器执行清理</li>
     *   <li>调用登出成功处理器</li>
     *   <li>如果不匹配，继续过滤器链</li>
     * </ol>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链
     * @throws IOException 输入输出异常
     * @throws ServletException Servlet异常
     */
    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (this.requiresLogout(request)) {
            LoginUser loginUser = OperatorUtils.getOperator();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Logging out [%s]", loginUser));
            }
            this.handler.logout(request, response, loginUser);
            this.logoutSuccessHandler.onLogoutSuccess(request, response, loginUser);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 检查请求是否需要登出处理
     * <p>
     * 使用Ant路径匹配器检查当前请求的URI是否匹配配置的登出URL。
     * 支持通配符和路径变量等灵活的匹配模式。
     * </p>
     *
     * <h3>匹配规则：</h3>
     * <ul>
     *   <li>精确匹配：/logout</li>
     *   <li>路径变量：/user/{id}/logout</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 如果请求匹配登出URL则返回true，否则返回false
     **/
    protected boolean requiresLogout(HttpServletRequest request) {
        if (antPathMatcher.match(this.filterProcessesUrl, request.getRequestURI())) {
            return true;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not match request to %s", this.filterProcessesUrl));
            }
            return false;
        }
    }
}
