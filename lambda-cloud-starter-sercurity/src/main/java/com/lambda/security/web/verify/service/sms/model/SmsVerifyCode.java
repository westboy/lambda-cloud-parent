package com.lambda.security.web.verify.service.sms.model;

import lombok.Getter;

/**
 * SmsVerifyCode
 *
 * @author Jin
 **/
@Getter
public class SmsVerifyCode<T> {

    /**
     * 验证码
     */
    private final T code;
    /**
     * 创建时间
     */
    private final long createTimeMillis;

    public SmsVerifyCode(T code) {
        this.code = code;
        this.createTimeMillis = System.currentTimeMillis();
    }
}
