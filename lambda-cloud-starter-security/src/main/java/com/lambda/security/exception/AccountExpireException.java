package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * 账户过期异常
 * <p>
 * 当用户账户已过期无法继续使用时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20003标识账户过期错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>用户账户超过有效期限</li>
 *   <li>临时账户到期</li>
 *   <li>试用期账户过期</li>
 *   <li>管理员设置的账户有效期到期</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户联系管理员续期</li>
 *   <li>引导用户进行账户续费</li>
 *   <li>记录过期账户的登录尝试</li>
 *   <li>提供账户恢复流程</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * if (user.isAccountExpired()) {
 *     throw new AccountExpireException("账户已过期，请联系管理员");
 * }
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginErrorCode#CODE_20003
 */
public class AccountExpireException extends AuthenticationException {

    /**
     * 构造账户过期异常
     * <p>
     * 使用指定的错误消息创建账户过期异常实例。
     * 自动设置错误码为CODE_20003。
     * </p>
     *
     * @param message 异常详细消息，描述账户过期的具体原因
     */
    public AccountExpireException(String message) {
        super(LoginErrorCode.CODE_20003, message);
    }
}
