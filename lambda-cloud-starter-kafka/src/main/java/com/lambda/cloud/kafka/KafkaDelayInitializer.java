package com.lambda.cloud.kafka;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.core.KafkaDelayPartition;
import com.lambda.cloud.kafka.service.KafkaDelayMonitorService;
import com.lambda.cloud.kafka.service.KafkaDelayTimeoutService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.CommandLineRunner;

/**
 * Kafka延时队列初始化器
 * <p>
 * 在应用启动时初始化延时队列的监控和超时服务。
 * 为每个分区创建DelayTopicPartition实例，并启动相应的监控和超时处理服务。
 *
 * @param kafkaDelayMonitorService 延时监控服务，负责监控延时消息
 * @param kafkaDelayTimeoutService 延时超时服务，负责处理超时的延时消息
 * @author jin
 * @since 1.0.0
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record KafkaDelayInitializer(
        KafkaDelayMonitorService kafkaDelayMonitorService, KafkaDelayTimeoutService kafkaDelayTimeoutService)
        implements CommandLineRunner {
    /**
     * 应用启动时执行的初始化逻辑
     * <p>
     * 为每个延时队列分区创建DelayTopicPartition实例，
     * 并启动对应的延时监控服务和超时处理服务。
     *
     * @param args 命令行参数（未使用）
     */
    @Override
    public void run(String... args) {
        for (int i = 0; i < KafkaDelayQueueConfigurer.SIZE; i++) {
            KafkaDelayPartition kafkaDelayPartition = new KafkaDelayPartition(i);
            kafkaDelayTimeoutService.execute(kafkaDelayPartition);
            kafkaDelayMonitorService.execute(kafkaDelayPartition);
        }
    }
}
