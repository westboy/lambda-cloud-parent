package com.lambda.cloud.sms.mock;

import com.lambda.cloud.sms.SmsISP;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import lombok.extern.slf4j.Slf4j;

/**
 * MockSmsMessageSender
 * @author Jin
 */
@Slf4j
public class MockSmsMessageSender implements SmsMessageSender {
    @Override
    public SmsSendResult sendVerifyCode(String phone, String code, int expire) {
        log.info("MockSendVerifyCode: phone={}, code={}, expire={}", phone, code, expire);
        return new SmsSendResult(true, "mock", "mock", code);
    }

    @Override
    public SmsSendResult sendMessage(String phone, String templateId, String params) {
        log.info("MockSendMessage: phone={}, templateId={}, params={}", phone, templateId, params);
        return new SmsSendResult(true, "mock", "mock", params);
    }

    @Override
    public SmsISP isp() {
        return SmsISP.MOCK;
    }
}
