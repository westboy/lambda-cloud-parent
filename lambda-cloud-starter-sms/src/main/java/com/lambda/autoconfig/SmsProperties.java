package com.lambda.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SmsProperties
 *
 * @author Jin
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lambda.sms")
public class SmsProperties {

    private Prod prod = new Prod();

    private AliYun aliyun = new AliYun();

    private Tencent tencent = new Tencent();

    @Getter
    @Setter
    public static class Prod {
        /**
         * 是否启用生产环境
         */
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class Tencent {
        /**
         * 是否启用腾讯短信服务
         */
        private boolean enabled;
        /**
         * 短信应用 SDK AppID
         */
        private int appId;
        /**
         * 短信应用 SDK AppKey
         */
        private String appKey;
        /**
         * 签名
         */
        private String smsSign;
        /**
         * 国家码，中国：86
         */
        private String nationCode = "86";
        /**
         * 短信验证码模板id
         */
        private int codeTemplateId;
    }


    @Getter
    @Setter
    public static class AliYun {
        /**
         * 是否启用阿里短信服务
         */
        private boolean enabled;
        /**
         * API支持的RegionID，如短信API的值为：cn-hangzhou
         */
        private String regionId = "cn-hangzhou";
        /**
         * 用于标识用户
         */
        private String accessKeyId;
        /**
         * 是用来验证用户的密钥
         */
        private String accessKeySecret;
        /**
         * 接口调用地址
         */
        private String domain = "dysmsapi.aliyuncs.com";
        /**
         * 短信签名
         */
        private String signName;
        /**
         * API版本号
         */
        private String version = "2017-05-25";
        /**
         * API名称
         */
        private SysAction action = SysAction.SENDSMS;
        /**
         * 短信验证码模板id
         */
        private String codeTemplateId;

        public enum SysAction {

            /**
             * 单发短信API
             */
            SENDSMS
        }
    }

}
