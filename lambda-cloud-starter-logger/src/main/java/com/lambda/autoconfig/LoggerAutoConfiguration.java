package com.lambda.autoconfig;

import com.lambda.cloud.logger.advices.OperationLoggerAdvice;
import com.lambda.cloud.logger.service.OperationService;
import com.lambda.cloud.logger.service.impl.DefaultOperationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 操作日志自动配置类。
 * <p>
 * 该配置类负责自动装配操作日志相关的组件，包括：
 * <ul>
 *     <li>操作日志切面 {@link OperationLoggerAdvice}</li>
 *     <li>操作日志服务 {@link OperationService}</li>
 * </ul>
 * <p>
 * 配置类在 Kafka 自动配置之后执行，确保 Kafka 相关依赖已准备就绪。
 * 仅在 Servlet Web 应用环境下生效。
 *
 * @author jpjoo
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggerAutoConfiguration {

    /**
     * 构造方法，记录初始化日志。
     */
    public LoggerAutoConfiguration() {
        log.trace("Lambda Logger Auto Configuration initializing...");
    }

    /**
     * Servlet Web 应用专用配置类。
     * <p>
     * 仅在 Servlet 类型的 Web 应用中生效，确保操作日志切面能够正确拦截 HTTP 请求。
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ServletConfiguration {

        /**
         * 创建操作日志切面 Bean。
         * <p>
         * 该切面负责拦截标注了 {@code @OperationLog} 注解的方法，
         * 记录操作日志信息并通过 {@link OperationService} 进行保存。
         *
         * @param operationService 操作日志服务实例
         * @return 操作日志切面实例
         */
        @Bean
        public OperationLoggerAdvice operationLoggerAdvice(OperationService operationService) {
            return new OperationLoggerAdvice(operationService);
        }
    }

    /**
     * 创建默认的操作日志服务 Bean。
     * <p>
     * 当容器中不存在 {@link OperationService} 实现时，
     * 自动创建默认实现 {@link DefaultOperationServiceImpl}。
     * 用户可以通过自定义 Bean 来覆盖此默认实现。
     *
     * @return 默认操作日志服务实例
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationService operationService() {
        return new DefaultOperationServiceImpl();
    }
}
