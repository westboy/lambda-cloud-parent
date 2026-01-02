package com.lambda.security.exception;

import com.lambda.security.LoginError;

/**
 * 账户锁定异常
 * <p>
 * 当用户账户被锁定无法进行登录操作时抛出此异常。
 * 继承自AuthenticationException，使用错误码CODE_20002标识账户锁定错误。
 * </p>
 *
 * <h3>触发场景：</h3>
 * <ul>
 *   <li>多次登录失败导致账户被锁定</li>
 *   <li>管理员手动锁定账户</li>
 *   <li>检测到异常登录行为</li>
 *   <li>安全策略触发的自动锁定</li>
 * </ul>
 *
 * <h3>安全机制：</h3>
 * <ul>
 *   <li>防止暴力破解攻击</li>
 *   <li>保护账户安全</li>
 *   <li>限制恶意登录尝试</li>
 *   <li>提供账户保护措施</li>
 * </ul>
 *
 * <h3>处理建议：</h3>
 * <ul>
 *   <li>提示用户等待锁定时间结束</li>
 *   <li>提供账户解锁申请流程</li>
 *   <li>记录锁定原因和时间</li>
 *   <li>通知用户锁定状态</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * if (user.isAccountLocked()) {
 *     throw new AccountLockedException("账户已被锁定，请稍后重试或联系管理员");
 * }
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see AuthenticationException
 * @see LoginError#ACCOUNT_LOCKED
 */
public class AccountLockedException extends AuthenticationException {

    /**
     * 构造账户锁定异常
     * <p>
     * 使用指定的错误消息创建账户锁定异常实例。
     * 自动设置错误码为CODE_20002。
     * </p>
     *
     * @param message 异常详细消息，描述账户锁定的具体原因
     */
    public AccountLockedException(String message) {
        super(LoginError.ACCOUNT_LOCKED.getCode(), message);
    }
}
