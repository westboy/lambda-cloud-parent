package com.lambda.cloud.kafka.service;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import com.lambda.cloud.kafka.KafkaDelayTemplate;
import com.lambda.cloud.kafka.core.KafkaDelayEntry;
import com.lambda.cloud.kafka.core.KafkaDelayPartition;
import com.lambda.cloud.kafka.core.KafkaDelayRecord;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

/**
 * 延时消息监控服务
 * <p>
 * 负责监控延时队列中的消息，处理延时消息的消费和重新分发。
 * 主要功能包括：
 * <ul>
 * <li>从延时主题中消费消息</li>
 * <li>检查消息是否已过期</li>
 * <li>将过期消息发送到原始主题</li>
 * <li>将未过期消息加入延时队列等待处理</li>
 * <li>管理消费者偏移量</li>
 * </ul>
 *
 * 该服务使用异步方式运行，每个分区对应一个独立的监控任务。
 *
 * @author jin
 * @since 1.0.0
 */
@Slf4j
public class KafkaDelayMonitorService {

    /**
     * 延时Kafka模板，用于发送延时消息
     */
    private KafkaDelayTemplate kafkaDelayTemplate;

    /**
     * Kafka服务器地址
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

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
     * 执行延时消息监控任务
     * <p>
     * 异步执行延时消息的监控和处理，包括：
     * <ul>
     * <li>创建Kafka消费者并订阅指定分区</li>
     * <li>持续轮询消息</li>
     * <li>处理每条消息（过期检查和队列管理）</li>
     * <li>提交消费者偏移量</li>
     * </ul>
     *
     * 该方法会无限循环运行，直到出现异常。
     *
     * @param kafkaDelayPartition 延时主题分区管理器
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Async(KafkaDelayQueueConfigurer.KAFKA_NO_BOUND_TASK_EXECUTOR)
    public void execute(KafkaDelayPartition kafkaDelayPartition) {
        Properties properties = getConsumerProperties();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.assign(Collections.singleton(
                    new TopicPartition(KafkaDelayRecord.DELAY_TOPIC, kafkaDelayPartition.getPartition())));
            do {
                kafkaDelayPartition.commit(consumer);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1L));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, String> record0 : records) {
                    execute0(record0, kafkaDelayPartition);
                }
            } while (true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 处理单条延时消息记录
     * <p>
     * 对消费到的每条消息进行处理：
     * <ul>
     * <li>创建DelayConsumerRecord包装原始记录</li>
     * <li>检查消息是否已过期</li>
     * <li>如果已过期：发送到原始主题并更新偏移量</li>
     * <li>如果未过期：加入延时队列等待后续处理</li>
     * </ul>
     *
     * @param record0             原始消费者记录
     * @param kafkaDelayPartition 延时主题分区管理器
     */
    private void execute0(ConsumerRecord<String, String> record0, KafkaDelayPartition kafkaDelayPartition) {
        AtomicInteger counter = kafkaDelayPartition.getCounter();
        DelayQueue<KafkaDelayEntry> queue = kafkaDelayPartition.getQueue();
        String topic = record0.topic();
        int partition = record0.partition();
        long offset = record0.offset();
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        KafkaDelayRecord kafkaDelayRecord = new KafkaDelayRecord(record0);
        if (kafkaDelayRecord.isExpired()) {
            kafkaDelayTemplate.send(kafkaDelayRecord.producerRecord());
            kafkaDelayPartition.seek(topicPartition, offset);
        } else {
            int remaining = kafkaDelayRecord.getDelayTime();
            long active = kafkaDelayRecord.getTopicExpireTime();
            KafkaDelayEntry delayed = new KafkaDelayEntry(active, record0, topicPartition, offset);
            if (queue.offer(delayed)) {
                counter.getAndIncrement();
                log.trace(
                        "Message are fetched from the topic! [{}-{}], remaining: {}s, active: {}",
                        topic,
                        partition,
                        remaining,
                        active);
            }
        }
    }

    /**
     * 获取Kafka消费者配置属性
     * <p>
     * 配置延时消息监控服务使用的Kafka消费者属性，包括：
     * <ul>
     * <li>服务器地址配置</li>
     * <li>禁用自动提交偏移量</li>
     * <li>字符串反序列化器</li>
     * <li>粘性分区分配策略</li>
     * <li>从最新偏移量开始消费</li>
     * <li>消费者组ID</li>
     * </ul>
     *
     * @return Kafka消费者配置属性
     */
    public Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, StickyAssignor.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "lambda-cloud-delay-consumer");
        return properties;
    }
}
