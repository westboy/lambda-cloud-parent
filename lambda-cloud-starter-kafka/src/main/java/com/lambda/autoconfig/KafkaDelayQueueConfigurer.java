package com.lambda.autoconfig;

import com.lambda.cloud.kafka.DelayKafkaInitializer;
import com.lambda.cloud.kafka.core.DelayConsumerRecord;
import com.lambda.cloud.kafka.service.DelayMonitorService;
import com.lambda.cloud.kafka.service.DelayTimeoutService;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * KafkaDelayQueueConfigurer
 *
 * @author jin
 */
@Configuration
@ConditionalOnProperty(value = "spring.kafka.delay.enabled", havingValue = "true")
public class KafkaDelayQueueConfigurer {

    public static final String KAFKA_NO_BOUND_TASK_EXECUTOR = "kafkaNoBoundTaskExecutor";
    public static final String KAFKA_MAXFIXED_TASK_EXECUTOR = "kafkaMaxFixedTaskExecutor";

    public static final int SIZE = 15;

    @Bean(name = KAFKA_NO_BOUND_TASK_EXECUTOR)
    public Executor kafkaNoboundTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程池大小
        executor.setCorePoolSize(SIZE);
        // 最大线程数
        executor.setMaxPoolSize(SIZE);
        // 队列容量
        executor.setQueueCapacity(1024);
        // 活跃时间
        executor.setKeepAliveSeconds(300);
        // 线程名字前缀
        executor.setThreadNamePrefix("lambda-cloud-delay-kafka-bound-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = KAFKA_MAXFIXED_TASK_EXECUTOR)
    public Executor kafkaMmaxfixedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程池大小
        executor.setCorePoolSize(SIZE);
        // 最大线程数
        executor.setMaxPoolSize(SIZE);
        // 队列容量
        executor.setQueueCapacity(1024);
        // 活跃时间
        executor.setKeepAliveSeconds(300);
        // 线程名字前缀
        executor.setThreadNamePrefix("lambda-cloud-delay-kafka-fixed-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public DelayMonitorService delayKafkaService() {
        return new DelayMonitorService();
    }

    @Bean
    public DelayTimeoutService timeOutMessageService() {
        return new DelayTimeoutService();
    }

    @Bean
    public DelayKafkaInitializer delayKafkaCommandRunner(
            DelayMonitorService delayMonitorService, DelayTimeoutService delayTimeoutService) {
        return new DelayKafkaInitializer(delayMonitorService, delayTimeoutService);
    }

    @Bean
    public NewTopic defaultDelayedTopics() {
        return TopicBuilder.name(DelayConsumerRecord.DELAY_TOPIC)
                .partitions(SIZE)
                .replicas(1)
                .build();
    }
}
