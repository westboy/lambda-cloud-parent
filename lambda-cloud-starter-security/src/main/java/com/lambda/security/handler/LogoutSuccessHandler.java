package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登出成功处理器接口
 * <p>
 * 定义用户登出成功时的处理逻辑，允许自定义登出成功后的响应行为。
 * 实现此接口可以自定义成功响应格式、页面跳转、统计记录等操作。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>处理登出成功事件</li>
 *   <li>自定义成功响应格式</li>
 *   <li>记录登出成功日志</li>
 *   <li>执行后续处理逻辑</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li>返回登出成功响应</li>
 *   <li>重定向到登录页面</li>
 *   <li>清理客户端状态</li>
 *   <li>更新用户在线状态</li>
 * </ul>
 *
 * <h3>处理内容：</h3>
 * <ul>
 *   <li>返回登出成功信息</li>
 *   <li>清理客户端Cookie</li>
 *   <li>记录登出成功日志</li>
 *   <li>执行页面跳转或响应</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
 *     @Override
 *     public void onLogoutSuccess(HttpServletRequest request,
 *                                HttpServletResponse response,
 *                                LoginUser loginUser) {
 *         // 记录登出成功日志
 *         log.info("用户 {} 成功退出登录", loginUser.getUsername());
 *
 *         // 返回成功响应
 *         response.setContentType("application/json;charset=UTF-8");
 *         response.getWriter().write("{\"success\":true,\"message\":\"退出成功\"}");
 *     }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see LogoutHandler
 * @see LoginUser
 */
public interface LogoutSuccessHandler {

    /**
     * 处理登出成功事件
     * <p>
     * 当用户登出成功时调用此方法，允许自定义登出成功后的处理逻辑。
     * 此方法在登出处理器执行完成后调用，用于返回响应或执行后续操作。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li>记录登出成功日志</li>
     *   <li>清理客户端状态</li>
     *   <li>返回成功响应</li>
     *   <li>执行页面跳转（如需要）</li>
     *   <li>更新统计信息</li>
     * </ol>
     *
     * @param request HTTP请求对象，包含登出请求信息
     * @param response HTTP响应对象，用于返回成功响应
     * @param loginUser 已登出的用户信息，包含用户详细数据
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     */
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException, ServletException;
}
