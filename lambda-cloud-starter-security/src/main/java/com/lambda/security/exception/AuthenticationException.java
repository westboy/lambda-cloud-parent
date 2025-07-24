package com.lambda.security.exception;

import cn.dev33.satoken.exception.SaTokenException;

/**
 * 认证异常基类
 * <p>
 * 所有认证相关异常的基础类，继承自SaTokenException。
 * 提供统一的认证异常处理机制，支持错误码、错误消息和异常链。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>统一认证异常处理</li>
 *   <li>支持错误码标识</li>
 *   <li>提供异常链追踪</li>
 *   <li>集成Sa-Token异常体系</li>
 * </ul>
 *
 * <h3>子异常类型：</h3>
 * <ul>
 *   <li>AccountExpireException - 账户过期异常</li>
 *   <li>AccountLockedException - 账户锁定异常</li>
 *   <li>BadCredentialsException - 凭据错误异常</li>
 *   <li>CaptchaException - 验证码异常</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * try {
 *     // 认证逻辑
 *     authenticate(username, password);
 * } catch (AuthenticationException e) {
 *     log.error("认证失败: {}", e.getMessage());
 *     return Result.error(e.getCode(), e.getMessage());
 * }
 * }</pre>
 *
 * @author Jin
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see SaTokenException
 * @see AccountExpireException
 * @see AccountLockedException
 */
public class AuthenticationException extends SaTokenException {

    /**
     * 构造认证异常
     * <p>
     * 使用指定的错误消息创建认证异常实例。
     * </p>
     *
     * @param message 异常详细消息
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * 构造带错误码的认证异常
     * <p>
     * 使用指定的错误码和错误消息创建认证异常实例。
     * 错误码用于标识具体的认证失败类型。
     * </p>
     *
     * @param code 错误码，标识异常类型
     * @param message 异常详细消息
     */
    public AuthenticationException(int code, String message) {
        super(code, message);
    }

    /**
     * 构造带异常链的认证异常
     * <p>
     * 使用指定的错误消息和原因异常创建认证异常实例。
     * 保留原始异常信息，便于问题追踪和调试。
     * </p>
     *
     * @param message 异常详细消息
     * @param cause 原因异常，导致此异常的底层异常
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
