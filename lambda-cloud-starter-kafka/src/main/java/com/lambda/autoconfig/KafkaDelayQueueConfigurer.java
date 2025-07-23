package com.lambda.autoconfig;

import com.lambda.cloud.kafka.KafkaDelayInitializer;
import com.lambda.cloud.kafka.core.KafkaDelayRecord;
import com.lambda.cloud.kafka.service.KafkaDelayMonitorService;
import com.lambda.cloud.kafka.service.KafkaDelayTimeoutService;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Kafka延时队列配置器
 * <p>
 * 当配置属性 spring.kafka.delay.enabled=true 时启用，提供：
 * <ul>
 *   <li>线程池执行器配置</li>
 *   <li>延时监控和超时服务配置</li>
 *   <li>延时队列初始化器配置</li>
 *   <li>默认延时主题配置</li>
 * </ul>
 *
 * @author jin
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.kafka.delay.enabled", havingValue = "true")
public class KafkaDelayQueueConfigurer {

    /**
     * 线程池执行器Bean名称常量
     */
    public static final String KAFKA_NO_BOUND_TASK_EXECUTOR = "kafkaNoBoundTaskExecutor";

    public static final String KAFKA_MAX_FIXED_TASK_EXECUTOR = "kafkaMaxFixedTaskExecutor";

    /**
     * 默认分区数量和线程池大小
     */
    public static final int SIZE = 15;

    /**
     * 配置无界任务执行器
     * <p>
     * 用于处理延时消息监控任务
     *
     * @return 无界任务执行器
     */
    @Bean(name = KAFKA_NO_BOUND_TASK_EXECUTOR)
    public Executor kafkaNoBoundTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(SIZE);
        executor.setMaxPoolSize(SIZE);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("lambda-cloud-delay-kafka-bound-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    /**
     * 配置固定大小任务执行器
     * <p>
     * 用于处理延时消息超时任务
     *
     * @return 固定大小任务执行器
     */
    @Bean(name = KAFKA_MAX_FIXED_TASK_EXECUTOR)
    public Executor kafkaMaxFixedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(SIZE);
        executor.setMaxPoolSize(SIZE);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("lambda-cloud-delay-kafka-fixed-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    /**
     * 配置延时监控服务
     *
     * @return 延时监控服务实例
     */
    @Bean
    public KafkaDelayMonitorService delayMonitorService() {
        return new KafkaDelayMonitorService();
    }

    /**
     * 配置延时超时服务
     *
     * @return 延时超时服务实例
     */
    @Bean
    public KafkaDelayTimeoutService delayTimeoutService() {
        return new KafkaDelayTimeoutService();
    }

    /**
     * 配置延时Kafka初始化器
     *
     * @param kafkaDelayMonitorService 延时监控服务
     * @param kafkaDelayTimeoutService 延时超时服务
     * @return 延时Kafka初始化器
     */
    @Bean
    public KafkaDelayInitializer delayKafkaInitializer(
            KafkaDelayMonitorService kafkaDelayMonitorService, KafkaDelayTimeoutService kafkaDelayTimeoutService) {
        return new KafkaDelayInitializer(kafkaDelayMonitorService, kafkaDelayTimeoutService);
    }

    /**
     * 配置默认延时主题
     *
     * @return 延时主题配置
     */
    @Bean
    public NewTopic defaultDelayedTopics() {
        return TopicBuilder.name(KafkaDelayRecord.DELAY_TOPIC)
                .partitions(SIZE)
                .replicas(1)
                .build();
    }
}
