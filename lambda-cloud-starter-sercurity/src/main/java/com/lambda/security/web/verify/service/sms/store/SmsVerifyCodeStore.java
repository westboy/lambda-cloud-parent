package com.lambda.security.web.verify.service.sms.store;


import com.lambda.security.web.verify.service.sms.SmsVerifyCode;

/**
 * VerifyCodeStore
 *
 * @author Jin
 **/
public interface SmsVerifyCodeStore<T> {

    /**
     * 获取验证码
     *
     * @param key mobile
     * @return 验证码
     */
    T generate(String key);

    /**
     * 验证验证码
     *
     * @param key  主键
     * @param code 验证码
     * @return 验证是否成功
     */
    boolean verify(String key, T code);

    /**
     * 获取验证码
     *
     * @param key String
     * @return 验证码
     */
    SmsVerifyCode<T> get(String key);

    /**
     * 验证是否可以重新发送
     *
     * @param key  String
     * @return boolean
     */
    boolean verifyReSend(String key);

    /**
     * 获取有效期，默认值: 300秒
     *
     * @return long
     */
    default long getPeriod() {
        return 300;
    }

    /**
     * 获取可重复发送间隔, 默认值：60秒
     *
     * @return long
     */
    default long getInterval() {
        return 60;
    }


}
