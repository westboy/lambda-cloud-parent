package com.lambda.security.exception;

import static com.lambda.security.LoginErrorCode.CODE_20006;

import lombok.Getter;

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
@Getter
public class CaptchaRequiredException extends AuthenticationException {

    private final int currentFailureTimes;
    private final int triggerTimes;

    public CaptchaRequiredException(String message, int currentFailureTimes, int triggerTimes) {
        super(CODE_20006, message);
        this.currentFailureTimes = currentFailureTimes;
        this.triggerTimes = triggerTimes;
    }
}
