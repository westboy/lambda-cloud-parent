package com.jingfang.cloud.logger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.logging.LogLevel;

import java.util.Collections;
import java.util.Map;

/**
 * LoggingExtendProperties
 *
 * @author jpjoo
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "logging")
public class LoggingExtendProperties {

    /**
     * 日志级别
     */
    Map<String, LogLevel> level = Collections.emptyMap();

    /**
     * 操作日志相关配置
     */
    @NestedConfigurationProperty
    Operation operation = new Operation();

    /**
     * 操作日志相关配置
     *
     */
    @Setter
    @Getter
    public static class Operation {
        @NestedConfigurationProperty
        Kafka kafka = new Kafka();

        @Getter
        @Setter
        public static class Kafka {
            /**
             * 是否通过Kafka收集日志
             */
            boolean enabled = false;
            /**
             * 日志主题
             */
            String topic;
        }
    }
}
