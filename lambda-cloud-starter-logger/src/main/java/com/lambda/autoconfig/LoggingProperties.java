package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Lambda 日志模块配置属性类。
 * <p>
 * 该类用于绑定以 {@code lambda.logging} 为前缀的配置属性，
 * 支持操作日志的各种配置选项，包括 Kafka 集成等。
 * <p>
 * 配置示例：
 * <pre>
 * lambda:
 *   logging:
 *     operation:
 *       kafka:
 *         enabled: true
 *         topic: operation-logs
 * </pre>
 *
 * @author jpjoo
 * @since 1.0.0
 */
@Getter
@Setter
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Spring Boot properties class with nested objects")
@ConfigurationProperties(prefix = "lambda.logging")
public class LoggingProperties {

    /**
     * 操作日志相关配置。
     * <p>
     * 包含操作日志的各种配置选项，如 Kafka 集成配置等。
     */
    @NestedConfigurationProperty
    private Operation operation = new Operation();

    /**
     * 操作日志配置类。
     * <p>
     * 定义操作日志相关的配置项，包括日志收集方式、存储配置等。
     */
    @Getter
    @Setter
    public static class Operation {

        /**
         * Kafka 相关配置。
         * <p>
         * 用于配置通过 Kafka 收集和传输操作日志的相关参数。
         */
        @NestedConfigurationProperty
        private Kafka kafka = new Kafka();

        /**
         * Kafka 配置类。
         * <p>
         * 定义 Kafka 相关的配置项，包括是否启用 Kafka、主题名称等。
         */
        @Getter
        @Setter
        public static class Kafka {
            
            /**
             * 是否启用通过 Kafka 收集日志。
             * <p>
             * 默认值为 {@code false}，即不启用 Kafka 日志收集。
             * 当设置为 {@code true} 时，操作日志将通过 Kafka 进行传输和处理。
             */
            private boolean enabled = false;
            
            /**
             * Kafka 日志主题名称。
             * <p>
             * 指定操作日志发送到的 Kafka 主题。
             * 仅在 {@link #enabled} 为 {@code true} 时生效。
             */
            private String topic;
        }
    }
}
