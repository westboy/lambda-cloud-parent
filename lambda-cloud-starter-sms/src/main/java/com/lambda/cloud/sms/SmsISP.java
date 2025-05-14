package com.lambda.cloud.sms;

/**
 * 短信运营商枚举类
 *
 * @author Jin
 */
public enum SmsISP {

    /**
     * 腾讯短信
     */
    TENCENT,

    /**
     * 阿里短信
     */
    ALIYUN,

    /**
     * 模拟短信
     */
    MOCK;

    public static SmsISP deserialize(String isp) {
        isp = isp.toUpperCase();
        for (SmsISP value : values()) {
            if (value.name().equals(isp)) {
                return value;
            }
        }
        return null;
    }
}
