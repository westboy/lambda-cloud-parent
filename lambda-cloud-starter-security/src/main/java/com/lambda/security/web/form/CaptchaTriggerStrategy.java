package com.lambda.security.web.form;

/**
 * 验证码动态触发策略接口
 *
 * <p>定义了基于登录失败次数动态触发验证码的策略标准。
 * 该接口与 FormLockingStrategy 配合使用，在达到账户锁定阈值之前，
 * 先触发验证码验证，提供额外的安全保护层。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>智能触发</strong> - 根据失败次数智能决定是否需要验证码</li>
 *   <li><strong>用户体验</strong> - 正常用户无需每次输入验证码</li>
 *   <li><strong>安全增强</strong> - 可疑行为时自动要求验证码</li>
 *   <li><strong>与锁定策略协同</strong> - 作为锁定之前的防护层</li>
 * </ul>
 *
 * @author jpjoo
 * @see FormLockingStrategy
 */
public interface CaptchaTriggerStrategy {

    /**
     * 检查是否需要验证码
     *
     * <p>根据用户的登录失败次数判断是否需要在登录时提供验证码。</p>
     *
     * @param username 用户名
     * @return true表示需要验证码，false表示不需要
     */
    boolean isCaptchaRequired(String username);

    /**
     * 获取当前失败次数
     *
     * @param username 用户名
     * @return 当前失败次数
     */
    int getFailureTimes(String username);

    /**
     * 获取触发验证码的失败次数阈值
     *
     * @return 触发阈值
     */
    int getTriggerTimes();

}
