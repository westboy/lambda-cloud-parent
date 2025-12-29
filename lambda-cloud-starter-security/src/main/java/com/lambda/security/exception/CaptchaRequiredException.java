package com.lambda.security.exception;

/**
 * 验证码必需异常
 *
 * <p>当用户登录失败次数达到动态触发阈值时，
 * 如果用户没有提供验证码，则抛出此异常。</p>
 *
 * <h3>异常信息</h3>
 * <ul>
 *   <li>包含当前失败次数</li>
 *   <li>包含触发阈值</li>
 *   <li>提示用户需要输入验证码</li>
 * </ul>
 *
 * @author jpjoo
 */
public class CaptchaRequiredException extends AuthenticationException {

    private final int currentFailureTimes;
    private final int triggerTimes;

    public CaptchaRequiredException(String message, int currentFailureTimes, int triggerTimes) {
        super(message);
        this.currentFailureTimes = currentFailureTimes;
        this.triggerTimes = triggerTimes;
    }

    public int getCurrentFailureTimes() {
        return currentFailureTimes;
    }

    public int getTriggerTimes() {
        return triggerTimes;
    }
}
