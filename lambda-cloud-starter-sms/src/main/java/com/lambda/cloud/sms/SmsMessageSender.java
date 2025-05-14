package com.lambda.cloud.sms;


import com.lambda.cloud.sms.model.SmsSendResult;

/**
 * 短信业务接口
 *
 * @author Jin
 */
public interface SmsMessageSender {

    /**
     * 发送短信验证码
     *
     * @param phone  手机号
     * @param code   验证码
     * @param expire 验证码有效时间，单位：分钟
     * @return 短信发送是否成功
     */
    SmsSendResult sendVerifyCode(String phone, String code, int expire);

    /**
     * 指定模板发送短信
     *
     * @param phone      手机号
     * @param templateId 模板id
     * @param params     模板参数，json字符串
     * @return 短信发送是否成功
     */
    SmsSendResult sendMessage(String phone, String templateId, String params);

    /**
     * 短信运营商
     *
     * @return 短信运营商
     */
    SmsISP isp();
}
