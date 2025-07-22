package com.lambda.cloud.kafka.service;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.DelayKafkaTemplate;
import com.lambda.cloud.kafka.core.DelayConsumerRecord;
import com.lambda.cloud.kafka.core.DelayEntry;
import com.lambda.cloud.kafka.core.DelayTopicPartition;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

/**
 * DelayTimeoutService
 *
 * @author jin
 */
@Slf4j
public class DelayTimeoutService {

    private DelayKafkaTemplate delayKafkaTemplate;

    @Autowired
    public void setDelayKafkaTemplate(DelayKafkaTemplate delayKafkaTemplate) {
        this.delayKafkaTemplate = delayKafkaTemplate;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Async(KafkaDelayQueueConfigurer.KAFKA_MAX_FIXED_TASK_EXECUTOR)
    public void execute(DelayTopicPartition delayTopicPartition) {
        AtomicInteger counter = delayTopicPartition.getCounter();
        DelayQueue<DelayEntry> queue = delayTopicPartition.getQueue();
        do {
            DelayEntry delayed = null;
            try {
                delayed = queue.take();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            if (Objects.nonNull(delayed)) {
                TopicPartition topicPartition = delayed.getTopicPartition();
                ConsumerRecord<String, String> consumerRecord = delayed.getConsumerRecord();
                long offset = delayed.getOffset();
                log.trace("Expired: {}, Offset: {}", consumerRecord.value(), offset);
                delayKafkaTemplate.send(new DelayConsumerRecord(consumerRecord).producerRecord());
                delayTopicPartition.seek(topicPartition, offset);
                counter.getAndDecrement();
            }
        } while (true);
    }
}
