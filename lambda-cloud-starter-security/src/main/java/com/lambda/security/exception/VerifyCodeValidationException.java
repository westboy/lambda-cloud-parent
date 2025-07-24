package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * 验证码验证异常
 * <p>
 * 当用户提交的验证码不正确或验证失败时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20004标识验证码错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>图形验证码输入错误</li>
 *   <li>短信验证码不匹配</li>
 *   <li>邮箱验证码错误</li>
 *   <li>验证码格式不正确</li>
 * </ul>
 *
 * <h3>安全机制：</h3>
 * <ul>
 *   <li>防止自动化攻击</li>
 *   <li>验证用户真实性</li>
 *   <li>限制恶意尝试</li>
 *   <li>保护系统安全</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户重新输入验证码</li>
 *   <li>记录验证失败次数</li>
 *   <li>提供验证码刷新功能</li>
 *   <li>实施验证频率限制</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * if (!captcha.getCode().equalsIgnoreCase(userInput)) {
 *     throw new VerifyCodeValidationException("验证码错误，请重新输入");
 * }
 * }</pre>
 *
 * @author Jin
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginErrorCode#CODE_20004
 */
public class VerifyCodeValidationException extends AuthenticationException {

    /**
     * 构造验证码验证异常
     * <p>
     * 使用指定的错误消息创建验证码验证异常实例。
     * 自动设置错误码为CODE_20004。
     * </p>
     *
     * @param message 异常详细消息，描述验证码验证失败的具体原因
     */
    public VerifyCodeValidationException(String message) {
        super(LoginErrorCode.CODE_20004, message);
    }
}
