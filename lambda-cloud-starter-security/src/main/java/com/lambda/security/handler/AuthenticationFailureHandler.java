package com.lambda.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理器接口
 * <p>
 * 定义用户认证失败时的处理逻辑，允许自定义认证失败后的响应行为。
 * 实现此接口可以自定义错误响应格式、日志记录、安全策略等。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>处理认证失败事件</li>
 *   <li>自定义错误响应格式</li>
 *   <li>记录认证失败日志</li>
 *   <li>实施安全防护策略</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li>用户名或密码错误</li>
 *   <li>账户被锁定或过期</li>
 *   <li>验证码验证失败</li>
 *   <li>其他认证异常情况</li>
 * </ul>
 *
 * <h3>实现建议：</h3>
 * <ul>
 *   <li>返回统一的错误响应格式</li>
 *   <li>记录失败尝试用于安全监控</li>
 *   <li>实施登录频率限制</li>
 *   <li>避免暴露敏感信息</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
 *     @Override
 *     public void onAuthenticationFailure(HttpServletRequest request,
 *                                        HttpServletResponse response,
 *                                        Exception exception) {
 *         // 记录失败日志
 *         log.warn("认证失败: {}", exception.getMessage());
 *
 *         // 返回错误响应
 *         response.setContentType("application/json;charset=UTF-8");
 *         response.getWriter().write("{\"error\":\"认证失败\"}");
 *     }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationSuccessHandler
 */
public interface AuthenticationFailureHandler {

    /**
     * 处理认证失败事件
     * <p>
     * 当用户认证失败时调用此方法，允许自定义失败后的处理逻辑。
     * 可以在此方法中记录日志、返回错误响应、实施安全策略等。
     * </p>
     *
     * <h3>处理内容：</h3>
     * <ul>
     *   <li>分析失败原因</li>
     *   <li>记录安全日志</li>
     *   <li>返回错误响应</li>
     *   <li>更新失败计数</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含认证请求信息
     * @param response HTTP响应对象，用于返回错误响应
     * @param exception 认证失败时抛出的异常，包含失败原因
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     */
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception)
            throws IOException, ServletException;
}
