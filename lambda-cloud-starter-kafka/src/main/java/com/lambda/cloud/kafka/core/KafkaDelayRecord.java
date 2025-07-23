package com.lambda.cloud.kafka.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

/**
 * 延时消费者记录处理类
 * <p>
 * 负责处理延时Kafka消息的核心逻辑，包括：
 * <ul>
 * <li>解析消息头中的延时信息</li>
 * <li>计算消息和主题的过期时间</li>
 * <li>判断消息是否已过期</li>
 * <li>构建用于重新发送的ProducerRecord</li>
 * </ul>
 *
 * @author jin
 * @since 1.0.0
 */
@Slf4j
public class KafkaDelayRecord {
    /**
     * 延时队列主题名称
     */
    public static final String DELAY_TOPIC = "lambda-cloud-delay";

    /**
     * 消息在当前延时级别主题中的过期时间头部键名
     */
    public static final String EXPIRE_TIME_IN_TOPIC = "expireTimeInTopic";

    /**
     * 消息最终过期时间头部键名
     */
    public static final String MESSAGE_EXPIRE_TIME = "msgExpireTime";

    /**
     * 消息原始目标主题头部键名
     */
    public static final String ORIGIN_TOPIC = "originTopic";

    /**
     * 原始消费者记录
     */
    private final ConsumerRecord<String, String> consumerRecord;

    /**
     * 消息最终过期时间（毫秒时间戳）
     */
    private final Long messageExpireTime;

    /**
     * 消息在当前延时级别的过期时间（毫秒时间戳）
     */
    private final Long topicExpireTime;

    /**
     * 当前时间戳（毫秒）
     */
    private final long millis;

    /**
     * 构造延时消费者记录
     *
     * @param consumerRecord 原始消费者记录
     * @throws IllegalArgumentException 当消费者记录为null时抛出
     */
    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW")
    public KafkaDelayRecord(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord == null) {
            throw new IllegalArgumentException("ConsumerRecord cannot be null");
        }
        this.millis = System.currentTimeMillis();
        this.consumerRecord = consumerRecord;
        this.topicExpireTime = calculateTopicExpireTime();
        this.messageExpireTime = calculateMessageExpireTime();
    }

    /**
     * 计算消息在当前延时级别主题中的过期时间
     *
     * @return 过期时间戳，如果头部不存在则返回null
     */
    private Long calculateTopicExpireTime() {
        Header header = this.consumerRecord.headers().lastHeader(EXPIRE_TIME_IN_TOPIC);
        return header != null ? Long.parseLong(new String(header.value(), UTF_8)) : null;
    }

    /**
     * 计算消息的最终过期时间
     *
     * @return 过期时间戳，如果头部不存在则返回null
     */
    private Long calculateMessageExpireTime() {
        Header header = this.consumerRecord.headers().lastHeader(MESSAGE_EXPIRE_TIME);
        return header != null ? Long.parseLong(new String(header.value(), UTF_8)) : null;
    }

    /**
     * 获取剩余延时时间
     *
     * @return 剩余延时时间（秒）
     */
    public int getDelayTime() {
        return (int) ((messageExpireTime - millis) / 1000L);
    }

    /**
     * 获取消息在当前延时级别主题中的过期时间
     *
     * @return 过期时间戳，如果为null则返回0
     */
    public long getTopicExpireTime() {
        return (long) ObjectUtils.defaultIfNull(topicExpireTime, 0L);
    }

    /**
     * 获取消息的最终过期时间
     *
     * @return 最终过期时间戳
     */
    public long getMessageExpireTime() {
        return messageExpireTime;
    }

    /**
     * 判断消息是否已过期
     * <p>
     * 消息过期的条件：
     * <ul>
     * <li>剩余延时时间小于1秒</li>
     * <li>在当前延时级别主题中的过期时间已到</li>
     * </ul>
     *
     * @return true表示消息已过期，false表示消息未过期
     */
    public boolean isExpired() {
        int delayTime = getDelayTime();
        return delayTime < 1 || topicExpireTime <= millis;
    }

    /**
     * 构建用于重新发送的生产者记录
     * <p>
     * 根据消息是否过期，决定发送到原始主题还是重新分配到延时队列：
     * <ul>
     * <li>如果消息已过期：发送到原始主题，移除延时相关头部</li>
     * <li>如果消息未过期：重新计算延时级别，更新过期时间头部</li>
     * </ul>
     *
     * @return 用于重新发送的ProducerRecord
     * @throws NullPointerException 当消息过期时间为null时抛出
     */
    public ProducerRecord<String, String> producerRecord() {
        String topic = consumerRecord.topic();
        String key = consumerRecord.key();
        String payload = consumerRecord.value();
        Headers headers = consumerRecord.headers();

        Objects.requireNonNull(messageExpireTime);
        headers.remove(EXPIRE_TIME_IN_TOPIC);
        int delay = (int) ((messageExpireTime - millis) / 1000L);
        Integer partition = null;
        // 如果延时消息已经过期
        if (delay <= 0) {
            topic = new String(headers.lastHeader(ORIGIN_TOPIC).value(), UTF_8);
            headers.remove(MESSAGE_EXPIRE_TIME);
            headers.remove(ORIGIN_TOPIC);
            log.trace("Payload {} has expired, exceeded:{}s, send: {}", payload, delay, topic);
        } else {
            KafkaDelayLevel kafkaDelayLevel = new KafkaDelayLevel(delay);
            partition = kafkaDelayLevel.getPartition();
            int level = kafkaDelayLevel.getLevel();
            long t = millis + level * 1000L;
            headers.add(EXPIRE_TIME_IN_TOPIC, String.valueOf(t).getBytes(StandardCharsets.UTF_8));
            log.trace("Payload {} has been reallocated, delay:{}s, send: {}-{}", payload, delay, topic, partition);
        }
        return new ProducerRecord<>(topic, partition, key, payload, headers);
    }
}
