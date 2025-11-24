package com.lambda.cloud.kafka.service;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.KafkaDelayTemplate;
import com.lambda.cloud.kafka.core.KafkaDelayEntry;
import com.lambda.cloud.kafka.core.KafkaDelayPartition;
import com.lambda.cloud.kafka.core.KafkaDelayRecord;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

/**
 * 延时消息超时处理服务
 * <p>
 * 负责处理延时队列中已到期的消息，将其重新发送到目标主题。
 * 主要功能包括：
 * <ul>
 * <li>从延时队列中取出已到期的消息</li>
 * <li>将到期消息重新发送到原始主题或重新分配延时级别</li>
 * <li>更新消费者偏移量</li>
 * <li>维护队列计数器</li>
 * </ul>
 *
 * 该服务使用阻塞队列的take()方法，会自动等待直到有消息到期。
 * 每个分区对应一个独立的超时处理任务。
 *
 * @author jin
 * @since 1.0.0
 */
@Slf4j
public class KafkaDelayTimeoutService {

    /**
     * 延时Kafka模板，用于重新发送到期的延时消息
     */
    private KafkaDelayTemplate kafkaDelayTemplate;

    /**
     * 设置延时Kafka模板
     *
     * @param kafkaDelayTemplate 延时Kafka模板实例
     */
    @Autowired
    public void setDelayKafkaTemplate(KafkaDelayTemplate kafkaDelayTemplate) {
        this.kafkaDelayTemplate = kafkaDelayTemplate;
    }

    /**
     * 执行延时消息超时处理任务
     * <p>
     * 异步执行延时消息的超时处理，包括：
     * <ul>
     * <li>从延时队列中阻塞获取已到期的消息</li>
     * <li>将到期消息重新发送（可能发送到原始主题或重新分配延时级别）</li>
     * <li>更新消费者偏移量以标记消息已处理</li>
     * <li>递减队列计数器</li>
     * </ul>
     *
     * 该方法会无限循环运行，使用DelayQueue的take()方法会自动阻塞等待
     * 直到有消息到期。如果线程被中断，会记录错误并重新设置中断状态。
     *
     * @param kafkaDelayPartition 延时主题分区管理器
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Async(KafkaDelayQueueConfigurer.KAFKA_MAX_FIXED_TASK_EXECUTOR)
    public void execute(KafkaDelayPartition kafkaDelayPartition) {
        AtomicInteger counter = kafkaDelayPartition.getCounter();
        DelayQueue<KafkaDelayEntry> queue = kafkaDelayPartition.getQueue();
        do {
            KafkaDelayEntry delayed = null;
            try {
                delayed = queue.take();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            if (Objects.nonNull(delayed)) {
                ConsumerRecord<String, String> consumerRecord = delayed.getConsumerRecord();
                long offset = delayed.getOffset();
                log.trace("Expired: {}, Offset: {}", consumerRecord.value(), offset);
                kafkaDelayTemplate.send(new KafkaDelayRecord(consumerRecord).producerRecord());
                // Mark as processed
                kafkaDelayPartition.removePendingOffset(offset);
                counter.getAndDecrement();
            }
        } while (true);
    }
}
