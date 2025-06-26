package com.lambda.security;

import cn.dev33.satoken.error.SaErrorCode;

public interface LoginCode extends SaErrorCode {

    /**
     * 登录类型错误
     */
    int CODE_20000 = 20000;

    /**
     * 账号或密码错误
     */
    int CODE_20001 = 20001;

    /**
     * 账号被锁定
     */
    int CODE_20002 = 20002;

    /**
     * 账号已过期
     */
    int CODE_20003 = 20003;

    /**
     * 验证码错误
     */
    int CODE_20004 = 20004;

    /**
     * 验证码过期
     */
    int CODE_20005 = 20005;
}
