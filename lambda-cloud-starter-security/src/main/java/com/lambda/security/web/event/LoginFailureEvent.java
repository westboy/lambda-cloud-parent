package com.lambda.security.web.event;

import org.springframework.context.ApplicationEvent;

/**
 * 登录失败事件
 *
 * <p>在用户登录认证失败后，由
 * {@link com.lambda.security.web.AbstractAuthenticationProcessingFilter} 发布。
 * 监听方（如日志中心）可据此记录登录失败审计日志，用于安全监控与失败统计。</p>
 *
 * <p><strong>安全说明：</strong>事件仅携带用户名、IP、登录类型与失败原因，
 * 不携带密码、凭证或 Token；失败原因面向审计，避免泄露可用于攻击的细节。</p>
 *
 * @author zx
 * @since 1.0.0
 * @see LoginSuccessEvent
 */
public class LoginFailureEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** 尝试登录的用户名（从登录参数中提取，可能为 null） */
    private final String username;

    /** 客户端 IP 地址 */
    private final String ip;

    /** 登录类型（form / sms / hmac / third 等，来源过滤器） */
    private final String loginType;

    /** 失败原因（异常信息） */
    private final String errorMessage;

    /**
     * 构造登录失败事件。
     *
     * @param source       事件源（通常为发布事件的过滤器）
     * @param username     尝试登录的用户名
     * @param ip           客户端 IP 地址
     * @param loginType    登录类型
     * @param errorMessage 失败原因
     */
    public LoginFailureEvent(Object source, String username, String ip, String loginType, String errorMessage) {
        super(source);
        this.username = username;
        this.ip = ip;
        this.loginType = loginType;
        this.errorMessage = errorMessage;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
