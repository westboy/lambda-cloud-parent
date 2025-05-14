package com.lambda.autoconfig;

import com.lambda.cloud.sms.SmsISP;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import com.lambda.cloud.sms.produce.AliYunSmsMessageSender;
import com.lambda.cloud.sms.produce.TencentSmsMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SmsAutoConfiguration
 *
 * @author Jin
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class SmsAutoConfiguration {

    public SmsAutoConfiguration() {
        log.trace("initializing...");
    }

    @Bean
    @ConditionalOnMissingBean(SmsProperties.class)
    public SmsProperties smsProperties() {
        return new SmsProperties();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "lambda.sms.prod", name = "enabled", havingValue = "true")
    public static class SmsProdAutoConfiguration {

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnProperty(prefix = "lambda.sms.aliyun", name = "enabled", havingValue = "true")
        public static class AliYunSmsAutoConfiguration {
            @Bean
            public SmsMessageSender aliYunSmsMessageSender(SmsProperties smsProperties) {
                return new AliYunSmsMessageSender(smsProperties);
            }
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnProperty(prefix = "lambda.sms.tencent", name = "enabled", havingValue = "true")
        public static class TencentSmsAutoConfiguration {

            @Bean
            public SmsMessageSender tencentSmsMessageSender(SmsProperties smsProperties) {
                return new TencentSmsMessageSender(smsProperties);
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "lambda.sms.prod", name = "enabled", havingValue = "false", matchIfMissing = true)
    public static class SmsMockAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean({SmsMessageSender.class})
        public SmsMessageSender defaultSmsService() {
            return new SmsMessageSender() {
                @Override
                public SmsSendResult sendVerifyCode(String phone, String code, int expire) {
                    return new SmsSendResult(true, "mock", "mock", "mock");
                }

                @Override
                public SmsSendResult sendMessage(String phone, String templateId, String params) {
                    return new SmsSendResult(true, "mock", "mock", "mock");
                }

                @Override
                public SmsISP isp() {
                    return SmsISP.MOCK;
                }
            };
        }
    }


}
