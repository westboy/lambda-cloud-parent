package com.lambda.security.exception;

import com.lambda.security.LoginErrorCode;

/**
 * 用户名未找到异常
 * <p>
 * 当系统中不存在指定的用户名时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20001标识用户名错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>用户输入的用户名在系统中不存在</li>
 *   <li>用户名格式正确但未注册</li>
 *   <li>用户账户已被删除</li>
 *   <li>用户名拼写错误</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li>不应直接暴露"用户名不存在"信息</li>
 *   <li>建议与密码错误使用相同的错误提示</li>
 *   <li>避免用户名枚举攻击</li>
 *   <li>统一返回通用错误信息</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户检查用户名拼写</li>
 *   <li>提供用户注册入口</li>
 *   <li>记录无效用户名尝试</li>
 *   <li>实施频率限制防止枚举</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * User user = userService.findByUsername(username);
 * if (user == null) {
 *     throw new UsernameNotFoundException("用户名或密码错误");
 * }
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginErrorCode#CODE_20001
 */
public class UsernameNotFoundException extends AuthenticationException {

    /**
     * 构造用户名未找到异常
     * <p>
     * 使用指定的错误消息创建用户名未找到异常实例。
     * 自动设置错误码为CODE_20001。
     * </p>
     *
     * @param message 异常详细消息，建议使用通用错误提示避免信息泄露
     */
    public UsernameNotFoundException(String message) {
        super(LoginErrorCode.CODE_20001, message);
    }
}
