package com.lambda.cloud.kafka;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.core.DelayTopicPartition;
import com.lambda.cloud.kafka.service.DelayMonitorService;
import com.lambda.cloud.kafka.service.DelayTimeoutService;
import org.springframework.boot.CommandLineRunner;

/**
 * @author jin
 */
public record DelayKafkaInitializer(DelayMonitorService delayMonitorService, DelayTimeoutService delayTimeoutService)
        implements CommandLineRunner {
    @Override
    public void run(String... args) {
        for (int i = 0; i < KafkaDelayQueueConfigurer.SIZE; i++) {
            DelayTopicPartition delayTopicPartition = new DelayTopicPartition(i);
            delayTimeoutService.execute(delayTopicPartition);
            delayMonitorService.execute(delayTopicPartition);
        }
    }
}
