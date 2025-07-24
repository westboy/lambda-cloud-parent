package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * 验证码过期异常
 * <p>
 * 当用户提交的验证码已过期时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20005标识验证码过期错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>图形验证码超过有效期</li>
 *   <li>短信验证码超时</li>
 *   <li>邮箱验证码过期</li>
 *   <li>动态验证码失效</li>
 * </ul>
 *
 * <h3>安全机制：</h3>
 * <ul>
 *   <li>限制验证码有效时间</li>
 *   <li>防止验证码重放攻击</li>
 *   <li>确保验证码时效性</li>
 *   <li>提高系统安全性</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户重新获取验证码</li>
 *   <li>清除过期的验证码缓存</li>
 *   <li>记录验证码过期事件</li>
 *   <li>提供验证码刷新功能</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * if (System.currentTimeMillis() > captcha.getExpireTime()) {
 *     throw new VerifyCodeExpireException("验证码已过期，请重新获取");
 * }
 * }</pre>
 *
 * @author Jin
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginErrorCode#CODE_20005
 */
public class VerifyCodeExpireException extends AuthenticationException {

    /**
     * 构造验证码过期异常
     * <p>
     * 使用指定的错误消息创建验证码过期异常实例。
     * 自动设置错误码为CODE_20005。
     * </p>
     *
     * @param message 异常详细消息，描述验证码过期的具体情况
     */
    public VerifyCodeExpireException(String message) {
        super(LoginErrorCode.CODE_20005, message);
    }
}
