package com.lambda.security.events;

import com.lambda.cloud.core.principal.LoginUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户登录事件
 * <p>
 * 继承自Spring的ApplicationEvent，用于在用户成功登录时发布事件通知。
 * 该事件包含登录用户信息、登录耗时、详细描述以及客户端网络信息，
 * 便于系统进行登录日志记录、安全审计和业务统计。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>记录用户登录成功事件</li>
 *   <li>提供登录耗时统计</li>
 *   <li>记录客户端网络信息</li>
 *   <li>支持自定义事件详情</li>
 * </ul>
 *
 * <h3>事件信息：</h3>
 * <ul>
 *   <li>登录用户：通过source获取LoginUser对象</li>
 *   <li>登录耗时：记录登录过程的时间消耗</li>
 *   <li>事件详情：可自定义的事件描述信息</li>
 *   <li>客户端地址：记录登录来源IP和端口</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 发布登录成功事件
 * UserLoginEvent event = new UserLoginEvent(loginUser, 150L, "192.168.1.100", 8080);
 * applicationEventPublisher.publishEvent(event);
 *
 * // 监听登录事件
 * @EventListener
 * public void handleUserLogin(UserLoginEvent event) {
 *     LoginUser user = (LoginUser) event.getSource();
 *     log.info("用户{}登录成功，耗时{}ms", user.getUsername(), event.getCast());
 * }
 * }</pre>
 *
 * @author Jin
 * @since 1.0.0
 * @see ApplicationEvent
 * @see LoginUser
 */
@Getter
public class UserLoginEvent extends ApplicationEvent {

    /**
     * 登录耗时（毫秒）
     * <p>
     * 记录从开始登录到登录成功的时间消耗，单位为毫秒。
     * 用于性能监控和登录过程优化分析。
     * </p>
     */
    private final long cast;

    /**
     * 事件详细描述
     * <p>
     * 登录事件的详细描述信息，默认为"登录成功"。
     * 可以根据具体业务需求自定义描述内容。
     * </p>
     */
    private final String details;

    /**
     * 客户端远程地址
     * <p>
     * 发起登录请求的客户端IP地址。
     * 用于安全审计和地理位置分析。
     * </p>
     */
    private final String remoteAddr;

    /**
     * 客户端远程端口
     * <p>
     * 发起登录请求的客户端端口号。
     * 用于网络连接分析和安全监控。
     * </p>
     */
    private final int remotePort;

    /**
     * 构造用户登录事件（默认描述）
     * <p>
     * 使用默认的"登录成功"描述创建登录事件。
     * 适用于标准的登录成功场景。
     * </p>
     *
     * @param loginUser 登录的用户对象
     * @param cast 登录耗时（毫秒）
     * @param remoteAddr 客户端IP地址
     * @param remotePort 客户端端口号
     */
    public UserLoginEvent(LoginUser loginUser, long cast, String remoteAddr, int remotePort) {
        this(loginUser, cast, "登录成功", remoteAddr, remotePort);
    }

    /**
     * 构造用户登录事件（自定义描述）
     * <p>
     * 使用自定义描述创建登录事件。
     * 允许根据具体业务场景自定义事件描述信息。
     * </p>
     *
     * @param loginUser 登录的用户对象，作为事件源
     * @param cast 登录耗时（毫秒）
     * @param details 事件详细描述
     * @param remoteAddr 客户端IP地址
     * @param remotePort 客户端端口号
     */
    public UserLoginEvent(LoginUser loginUser, long cast, String details, String remoteAddr, int remotePort) {
        super(loginUser);
        this.cast = cast;
        this.details = details;
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
    }
}
