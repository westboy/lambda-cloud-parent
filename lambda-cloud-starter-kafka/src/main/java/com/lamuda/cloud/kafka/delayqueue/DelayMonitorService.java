package com.lambda.cloud.kafka.delayqueue;

import com.lambda.autoconfig.KafkaDelayQueueConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lambda.cloud.kafka.delayqueue.DelayConsumerRecord.DELAY_TOPIC;


/**
 * DelayMonitorService
 *
 * @author jin
 */
@Slf4j
public class DelayMonitorService {

    private DelayKafkaTemplate delayKafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Autowired
    public void setDelayKafkaTemplate(DelayKafkaTemplate delayKafkaTemplate) {
        this.delayKafkaTemplate = delayKafkaTemplate;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Async(KafkaDelayQueueConfigurer.KAFKA_NO_BOUND_TASK_EXECUTOR)
    public void execute(DelayTopicPartition delayTopicPartition) {
        Properties properties = getConsumerProperties();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.assign(Collections.singleton(new TopicPartition(DELAY_TOPIC, delayTopicPartition.getPartition())));
            do {
                delayTopicPartition.commit(consumer);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1L));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, String> record0 : records) {
                    execute0(record0, delayTopicPartition);
                }
            } while (true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void execute0(ConsumerRecord<String, String> record0, DelayTopicPartition delayTopicPartition) {
        AtomicInteger counter = delayTopicPartition.getCounter();
        DelayQueue<DelayEntry> queue = delayTopicPartition.getQueue();
        String topic = record0.topic();
        int partition = record0.partition();
        long offset = record0.offset();
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        DelayConsumerRecord delayConsumerRecord = new DelayConsumerRecord(record0);
        if (delayConsumerRecord.isExpired()) {
            delayKafkaTemplate.send(delayConsumerRecord.producerRecord());
            delayTopicPartition.seek(topicPartition, offset);
        } else {
            int remaining = delayConsumerRecord.getDelayTime();
            long active = delayConsumerRecord.getTopicExpireTime();
            DelayEntry delayed = new DelayEntry(active, record0, topicPartition, offset);
            if (queue.offer(delayed)) {
                counter.getAndIncrement();
                log.trace("Message are fetched from the topic! [{}-{}], remaining: {}s, active: {}",
                        topic, partition, remaining, active);
            }
        }
    }


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
