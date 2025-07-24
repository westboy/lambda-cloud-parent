package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证成功处理器接口
 * <p>
 * 定义用户认证成功时的处理逻辑，允许自定义认证成功后的响应行为。
 * 实现此接口可以自定义成功响应格式、用户信息处理、会话管理等。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>处理认证成功事件</li>
 *   <li>自定义成功响应格式</li>
 *   <li>记录登录成功日志</li>
 *   <li>更新用户登录信息</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li>用户登录成功后的处理</li>
 *   <li>返回Token或会话信息</li>
 *   <li>记录登录日志和统计</li>
 *   <li>更新用户最后登录时间</li>
 * </ul>
 *
 * <h3>实现建议：</h3>
 * <ul>
 *   <li>返回统一的成功响应格式</li>
 *   <li>记录登录成功日志</li>
 *   <li>更新用户登录状态</li>
 *   <li>处理会话和Token管理</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
 *     @Override
 *     public void onAuthenticationSuccess(HttpServletRequest request,
 *                                        HttpServletResponse response,
 *                                        LoginUser loginUser) {
 *         // 记录登录日志
 *         log.info("用户 {} 登录成功", loginUser.getUsername());
 *
 *         // 返回成功响应
 *         response.setContentType("application/json;charset=UTF-8");
 *         response.getWriter().write("{\"success\":true,\"token\":\"xxx\"}");
 *     }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationFailureHandler
 * @see LoginUser
 */
public interface AuthenticationSuccessHandler {

    /**
     * 认证成功默认处理方法
     * <p>
     * 提供默认的认证成功处理逻辑，先调用具体的成功处理方法，
     * 然后继续执行过滤器链。适用于需要继续处理请求的场景。
     * </p>
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>调用具体的认证成功处理方法</li>
     *   <li>继续执行过滤器链</li>
     *   <li>允许请求继续传递</li>
     * </ol>
     *
     * @param request HTTP请求对象，包含认证请求信息
     * @param response HTTP响应对象，用于返回成功响应
     * @param chain 过滤器链，用于继续处理请求
     * @param loginUser 认证成功的用户信息
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     */
    default void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser)
            throws IOException, ServletException {
        this.onAuthenticationSuccess(request, response, loginUser);
        chain.doFilter(request, response);
    }

    /**
     * 处理认证成功事件
     * <p>
     * 当用户认证成功时调用此方法，允许自定义成功后的处理逻辑。
     * 可以在此方法中记录日志、返回成功响应、更新用户信息等。
     * </p>
     *
     * <h3>处理内容：</h3>
     * <ul>
     *   <li>记录登录成功日志</li>
     *   <li>返回成功响应数据</li>
     *   <li>更新用户登录状态</li>
     *   <li>处理会话和Token</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含认证请求信息
     * @param response HTTP响应对象，用于返回成功响应
     * @param loginUser 认证成功的用户信息，包含用户详细数据
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     */
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException, ServletException;
}
