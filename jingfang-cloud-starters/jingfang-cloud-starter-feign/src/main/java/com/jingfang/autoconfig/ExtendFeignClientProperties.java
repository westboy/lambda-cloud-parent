package com.jingfang.autoconfig;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClientProperties;

/**
 * Feign相关配置
 *
 * @author westboy
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(prefix = "spring.cloud.openfeign.client")
public class ExtendFeignClientProperties extends FeignClientProperties {

    String basePackage = "com.jingfang.cloud";

    Retry retry = new Retry();

    Ssl ssl = new Ssl();

    @Getter
    @Setter
    public static class Retry {
        /**
         * 是否开启重试
         */
        boolean enabled = false;
        /**
         * 默认重试次数
         */
        int maxAttempts = 3;
    }

    @Getter
    @Setter
    public static class Ssl {
        /**
         * 是否开启SSL
         */
        private boolean enabled = false;
        /**
         * 证书地址
         */
        private String cert = "cert.jks";
        /**
         * 证书密钥
         */
        private String password;
    }

}
