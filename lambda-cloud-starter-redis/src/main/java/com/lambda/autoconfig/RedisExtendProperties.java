package com.lambda.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jin
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisExtendProperties {
    /**
     * redis运行模式
     */
    private Mode mode = Mode.STANDALONE;

    /**
     * 运行模式
     */
    public enum Mode {
        /**
         * 哨兵模式
         */
        SENTINEL,
        /**
         * 集群模式
         */
        CLUSTER,
        /**
         * 单机模式
         */
        STANDALONE
    }

}
