package com.lambda.autoconfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * WebClient配置属性
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "lambda.webclient")
public class WebClientProperties {

    /**
     * 是否启用WebClient
     */
    private boolean enabled = true;

    /**
     * 默认配置
     */
    @NestedConfigurationProperty
    private ClientConfig defaultConfig = new ClientConfig();

    /**
     * 客户端配置映射
     */
    private Map<String, ClientConfig> clients = new HashMap<>();

    /**
     * 全局请求头
     */
    private Map<String, String> defaultHeaders = new HashMap<>();

    /**
     * 是否启用指标监控
     */
    private boolean metricsEnabled = true;

    /**
     * 是否启用请求日志
     */
    private boolean loggingEnabled = true;

    /**
     * 客户端配置
     */
    @Data
    public static class ClientConfig {
        /**
         * 基础URL
         */
        private String baseUrl;

        /**
         * 连接超时时间
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * 读取超时时间
         */
        private Duration readTimeout = Duration.ofSeconds(30);

        /**
         * 写入超时时间
         */
        private Duration writeTimeout = Duration.ofSeconds(30);

        /**
         * 响应超时时间
         */
        private Duration responseTimeout = Duration.ofSeconds(30);

        /**
         * 最大内存大小
         */
        private int maxInMemorySize = 1024 * 1024; // 1MB

        /**
         * 连接池配置
         */
        @NestedConfigurationProperty
        private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

        /**
         * 重试配置
         */
        @NestedConfigurationProperty
        private RetryConfig retry = new RetryConfig();

        /**
         * SSL配置
         */
        @NestedConfigurationProperty
        private SslConfig ssl = new SslConfig();

        /**
         * 请求头配置
         */
        private Map<String, String> headers = new HashMap<>();

        /**
         * 是否启用HMAC认证
         */
        private boolean hmacEnabled = false;

        /**
         * HMAC配置
         */
        @NestedConfigurationProperty
        private HmacConfig hmac = new HmacConfig();
    }

    /**
     * 连接池配置
     */
    @Data
    public static class ConnectionPoolConfig {
        /**
         * 最大连接数
         */
        private int maxConnections = 500;

        /**
         * 最大空闲时间
         */
        private Duration maxIdleTime = Duration.ofSeconds(30);

        /**
         * 最大生命周期
         */
        private Duration maxLifeTime = Duration.ofMinutes(30);

        /**
         * 等待获取连接的超时时间
         */
        private Duration acquireTimeout = Duration.ofSeconds(45);
    }

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 重试间隔
         */
        private Duration backoff = Duration.ofMillis(1000);

        /**
         * 最大重试间隔
         */
        private Duration maxBackoff = Duration.ofSeconds(10);

        /**
         * 重试倍数
         */
        private double multiplier = 2.0;

        /**
         * 需要重试的状态码
         */
        private int[] retryableStatusCodes = {500, 502, 503, 504};
    }


    /**
     * SSL配置
     */
    @Data
    public static class SslConfig {
        /**
         * 是否启用SSL
         */
        private boolean enabled = false;

        /**
         * 是否信任所有证书
         */
        private boolean trustAll = false;

        /**
         * 密钥库路径
         */
        private String keyStore;

        /**
         * 密钥库密码
         */
        private String keyStorePassword;

        /**
         * 信任库路径
         */
        private String trustStore;

        /**
         * 信任库密码
         */
        private String trustStorePassword;
    }

    /**
     * HMAC配置
     */
    @Data
    public static class HmacConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        /**
         * 应用ID
         */
        private String appId;

        /**
         * 密钥
         */
        private String secret;
    }
}
