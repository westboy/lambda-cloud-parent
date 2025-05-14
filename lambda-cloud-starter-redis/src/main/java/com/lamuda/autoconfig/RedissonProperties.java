package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author westboy
 */
@Data
@ConfigurationProperties(prefix = "spring.data.redis.redisson")
public class RedissonProperties {
    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 最小空闲订阅连接数
     */
    private int subscriptionConnectionMinimumIdleSize = 1;
    /**
     * 订阅连接最大池大小
     */
    private int subscriptionConnectionPoolSize = 50;
    /**
     * 最小空闲连接数
     */
    private int connectionMinimumIdleSize = 24;
    /**
     * 连接最大池大小
     */
    private int connectionPoolSize = 64;
    /**
     * 连接的数据库索引
     */
    private int database = 0;
    /**
     * PING命令发送间隔(毫秒)
     */
    private int pingConnectionInterval = 30000;
}
