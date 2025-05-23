package com.lambda.cloud.kafka;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.core.DelayTopicPartition;
import com.lambda.cloud.kafka.service.DelayMonitorService;
import com.lambda.cloud.kafka.service.DelayTimeoutService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.CommandLineRunner;

/**
 * @author jin
 */
public class DelayKafkaInitializer implements CommandLineRunner {

    private final DelayMonitorService delayMonitorService;
    private final DelayTimeoutService delayTimeoutService;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public DelayKafkaInitializer(DelayMonitorService delayMonitorService, DelayTimeoutService delayTimeoutService) {
        this.delayMonitorService = delayMonitorService;
        this.delayTimeoutService = delayTimeoutService;
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < KafkaDelayQueueConfigurer.SIZE; i++) {
            DelayTopicPartition delayTopicPartition = new DelayTopicPartition(i);
            delayTimeoutService.execute(delayTopicPartition);
            delayMonitorService.execute(delayTopicPartition);
        }
    }
}
