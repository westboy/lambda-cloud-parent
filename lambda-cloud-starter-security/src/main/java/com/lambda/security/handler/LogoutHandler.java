package com.lambda.security.handler;

import com.lambda.cloud.core.principal.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登出处理器接口
 * <p>
 * 定义用户登出时的处理逻辑，允许自定义登出过程中的清理和处理行为。
 * 实现此接口可以自定义会话清理、缓存清除、日志记录等操作。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>处理用户登出事件</li>
 *   <li>清理用户会话信息</li>
 *   <li>清除相关缓存数据</li>
 *   <li>记录登出操作日志</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li>用户主动退出登录</li>
 *   <li>会话超时自动登出</li>
 *   <li>管理员强制用户下线</li>
 *   <li>安全策略触发的登出</li>
 * </ul>
 *
 * <h3>处理内容：</h3>
 * <ul>
 *   <li>清除用户Token和会话</li>
 *   <li>清理用户相关缓存</li>
 *   <li>记录登出时间和原因</li>
 *   <li>通知相关系统用户下线</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomLogoutHandler implements LogoutHandler {
 *     @Override
 *     public void logout(HttpServletRequest request,
 *                       HttpServletResponse response,
 *                       LoginUser loginUser) {
 *         // 清除用户缓存
 *         cacheService.clearUserCache(loginUser.getId());
 *
 *         // 记录登出日志
 *         log.info("用户 {} 退出登录", loginUser.getUsername());
 *
 *         // 清理会话信息
 *         sessionService.invalidateSession(loginUser.getId());
 *     }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see LogoutSuccessHandler
 * @see LoginUser
 */
public interface LogoutHandler {

    /** 当前请求已完成登录状态检查的标记。 */
    String LOGIN_CHECKED_ATTRIBUTE = LogoutHandler.class.getName() + ".LOGIN_CHECKED";

    /** 当前请求解析出的活跃登录逻辑。 */
    String ACTIVE_STP_LOGIC_ATTRIBUTE = LogoutHandler.class.getName() + ".ACTIVE_STP_LOGIC";

    /**
     * 处理用户登出事件
     * <p>
     * 当用户登出时调用此方法，执行登出相关的清理和处理操作。
     * 此方法在登出成功处理器之前执行，用于清理用户相关的数据和状态。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li>验证用户登出权限</li>
     *   <li>清除用户会话和Token</li>
     *   <li>清理用户相关缓存</li>
     *   <li>记录登出操作日志</li>
     *   <li>通知相关系统</li>
     * </ol>
     *
     * @param request HTTP请求对象，包含登出请求信息
     * @param response HTTP响应对象，用于设置响应信息
     * @param loginUser 要登出的用户信息，包含用户详细数据
     */
    void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser);
}
