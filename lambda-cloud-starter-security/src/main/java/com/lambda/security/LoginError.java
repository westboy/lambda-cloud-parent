package com.lambda.security;

import com.lambda.cloud.core.exception.model.ErrorCode;

/**
 * 登录错误码定义接口
 * <p>
 * 定义Lambda Cloud安全模块中所有登录相关的错误码常量。
 * 继承自Sa-Token的SaErrorCode接口，提供统一的错误码规范。
 * </p>
 *
 * <h3>错误码规范：</h3>
 * <ul>
 *   <li>20000-20099: 登录认证相关错误</li>
 *   <li>错误码采用递增方式分配</li>
 *   <li>每个错误码都有明确的业务含义</li>
 *   <li>便于前端进行错误处理和用户提示</li>
 * </ul>
 *
 * @author Jin
 */
public enum LoginError implements ErrorCode {
    /** 业务异常：业务异常 */
    BUSINESS_ERROR(20000, "业务错误"),

    /** 账号或密码错误：为了安全考虑，不区分用户名或密码错误 */
    ACCOUNT_PASSWORD_ERROR(20001, "账号或密码错误"),

    /** 账号被锁定：因多次登录失败或安全原因被系统锁定 */
    ACCOUNT_LOCKED(20002, "账号被锁定"),

    /** 账号已过期：超过有效期，需要联系管理员重新激活 */
    ACCOUNT_EXPIRED(20003, "账号已过期"),

    /** 验证码错误：用户输入验证码与系统生成的不匹配 */
    CAPTCHA_ERROR(20004, "验证码错误"),

    /** 验证码过期：验证码超过有效期，需重新获取 */
    CAPTCHA_EXPIRED(20005, "验证码过期"),

    /** 需要验证码：请求中未包含验证码参数 */
    CAPTCHA_REQUIRED(20006, "需要验证码"),

    /** 登录类型错误：用户选择的登录方式不被系统支持或配置错误 */
    LOGIN_TYPE_ERROR(20099, "登录类型错误");

    private final int code;
    private final String message;

    LoginError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
