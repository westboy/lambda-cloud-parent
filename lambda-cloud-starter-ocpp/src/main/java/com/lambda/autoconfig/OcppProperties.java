package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OCPP 协议 starter 配置属性。
 * <p>配置前缀:{@code lambda.ocpp}。</p>
 */
@Data
@ConfigurationProperties(prefix = "lambda.ocpp")
public class OcppProperties {

    /**
     * 是否启用 OCPP 自动配置(注册 action registry 与编解码器)。
     */
    private boolean enabled = true;
}
