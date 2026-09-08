package com.lambda.security.web.event;

import com.lambda.cloud.core.principal.LoginUser;
import org.springframework.context.ApplicationEvent;

/**
 * 登录成功事件
 *
 * <p>在用户通过任一认证方式（表单、短信、HMAC、第三方等）登录成功后，由
 * {@link com.lambda.security.web.AbstractAuthenticationProcessingFilter} 发布。
 * 监听方（如日志中心）可据此记录登录成功审计日志。</p>
 *
 * <p><strong>安全说明：</strong>事件仅携带用户名、IP、登录类型等非敏感标识，
 * 不携带密码、凭证或 Token，避免在事件/日志链路泄露敏感信息。</p>
 *
 * @author zx
 * @since 1.0.0
 * @see LoginFailureEvent
 */
public class LoginSuccessEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** 登录成功的用户名 */
    private final String username;

    /** 登录成功的用户主体（含设备/租户等信息，供监听方补全日志字段） */
    private final LoginUser loginUser;

    /** 客户端 IP 地址 */
    private final String ip;

    /** 登录类型（form / sms / hmac / third 等，来源过滤器） */
    private final String loginType;

    /** 租户 ID（若可获取） */
    private final String tenantId;

    /**
     * 构造登录成功事件。
     *
     * @param source    事件源（通常为发布事件的过滤器）
     * @param loginUser 登录成功的用户主体
     * @param ip        客户端 IP 地址
     * @param loginType 登录类型
     */
    public LoginSuccessEvent(Object source, LoginUser loginUser, String ip, String loginType) {
        super(source);
        this.loginUser = loginUser;
        this.username = loginUser != null ? loginUser.getName() : null;
        this.tenantId = loginUser != null ? loginUser.getTenantId() : null;
        this.ip = ip;
        this.loginType = loginType;
    }

    public String getUsername() {
        return username;
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public String getIp() {
        return ip;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getTenantId() {
        return tenantId;
    }
}
