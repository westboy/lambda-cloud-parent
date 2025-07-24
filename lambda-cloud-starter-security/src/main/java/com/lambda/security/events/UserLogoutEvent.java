package com.lambda.security.events;

import com.lambda.cloud.core.principal.LoginUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户登出事件
 * <p>
 * 继承自Spring的ApplicationEvent，用于在用户登出时发布事件通知。
 * 该事件包含登出用户信息、登出耗时、详细描述以及客户端IP地址，
 * 便于系统进行登出日志记录、会话管理和安全审计。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>记录用户登出事件</li>
 *   <li>提供登出耗时统计</li>
 *   <li>记录客户端IP信息</li>
 *   <li>支持自定义事件详情</li>
 * </ul>
 *
 * <h3>事件信息：</h3>
 * <ul>
 *   <li>登出用户：通过source获取LoginUser对象</li>
 *   <li>登出耗时：记录登出过程的时间消耗</li>
 *   <li>事件详情：可自定义的事件描述信息</li>
 *   <li>客户端IP：记录登出来源IP地址</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 发布登出事件
 * UserLogoutEvent event = new UserLogoutEvent(loginUser, 50L);
 * event.setIpaddress("192.168.1.100");
 * applicationEventPublisher.publishEvent(event);
 *
 * // 监听登出事件
 * @EventListener
 * public void handleUserLogout(UserLogoutEvent event) {
 *     LoginUser user = (LoginUser) event.getSource();
 *     log.info("用户{}登出成功，耗时{}ms", user.getUsername(), event.getCast());
 * }
 * }</pre>
 *
 * @author Jin
 * @since 1.0.0
 * @see ApplicationEvent
 * @see LoginUser
 */
@Getter
public class UserLogoutEvent extends ApplicationEvent {

    /**
     * 登出耗时（毫秒）
     * <p>
     * 记录从开始登出到登出完成的时间消耗，单位为毫秒。
     * 用于性能监控和登出过程优化分析。
     * </p>
     */
    private final long cast;

    /**
     * 事件详细描述
     * <p>
     * 登出事件的详细描述信息，默认为"用户退出登录"。
     * 可以根据具体业务需求自定义描述内容。
     * </p>
     */
    private final String details;

    /**
     * 客户端IP地址
     * <p>
     * 发起登出请求的客户端IP地址。
     * 用于安全审计和地理位置分析。
     * 可通过setter方法设置。
     * </p>
     */
    @Setter
    private String ipaddress;

    /**
     * 构造用户登出事件（默认描述）
     * <p>
     * 使用默认的"用户退出登录"描述创建登出事件。
     * 适用于标准的用户登出场景。
     * </p>
     *
     * @param loginUser 登出的用户对象
     * @param cast 登出耗时（毫秒）
     */
    public UserLogoutEvent(LoginUser loginUser, long cast) {
        this(loginUser, cast, "用户退出登录");
    }

    /**
     * 构造用户登出事件（自定义描述）
     * <p>
     * 使用自定义描述创建登出事件。
     * 允许根据具体业务场景自定义事件描述信息。
     * </p>
     *
     * @param loginUser 登出的用户对象，作为事件源
     * @param cast 登出耗时（毫秒）
     * @param details 事件详细描述
     */
    public UserLogoutEvent(LoginUser loginUser, long cast, String details) {
        super(loginUser);
        this.cast = cast;
        this.details = details;
    }
}
