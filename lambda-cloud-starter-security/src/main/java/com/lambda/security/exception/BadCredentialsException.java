package com.lambda.security.exception;

import com.lambda.security.LoginError;

/**
 * 凭据错误异常
 * <p>
 * 当用户提供的登录凭据（用户名、密码等）不正确时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20001标识凭据错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>用户名不存在</li>
 *   <li>密码错误</li>
 *   <li>用户名和密码不匹配</li>
 *   <li>凭据格式不正确</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li>不应暴露具体的错误原因（如用户名不存在还是密码错误）</li>
 *   <li>统一返回"用户名或密码错误"避免信息泄露</li>
 *   <li>记录失败尝试用于安全监控</li>
 *   <li>配合账户锁定机制防止暴力破解</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户检查用户名和密码</li>
 *   <li>提供密码重置功能</li>
 *   <li>记录登录失败次数</li>
 *   <li>实施登录频率限制</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
 *     throw new BadCredentialsException("用户名或密码错误");
 * }
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginError#ACCOUNT_PASSWORD_ERROR
 */
public class BadCredentialsException extends AuthenticationException {

    /**
     * 构造凭据错误异常
     * <p>
     * 使用指定的错误消息创建凭据错误异常实例。
     * 自动设置错误码为CODE_20001。
     * </p>
     *
     * @param message 异常详细消息，建议使用通用的错误提示避免信息泄露
     */
    public BadCredentialsException(String message) {
        super(LoginError.ACCOUNT_PASSWORD_ERROR.getCode(), message);
    }
}
