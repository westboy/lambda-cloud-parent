package com.jingfang.cloud.kafka.delayqueue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author jin
 */
@Slf4j
public class DelayConsumerRecord {
    /**
     * 延时队列名称前缀
     */
    public static final String DELAY_TOPIC = "jingfang-cloud-delay";
    /**
     * 消息在该延时队列中的过期时间
     */
    public static final String EXPIRE_TIME_IN_TOPIC = "expireTimeInTopic";
    /**
     * 消息过期时间
     */
    public static final String MESSAGE_EXPIRE_TIME = "msgExpireTime";
    /**
     * 消息原始的topic
     */
    public static final String ORIGIN_TOPIC = "originTopic";

    private final ConsumerRecord<String, String> consumerRecord;
    private final Long messageExpireTime;
    private final Long topicExpireTime;
    private final long millis;

    public DelayConsumerRecord(ConsumerRecord<String, String> consumerRecord) {
        this.millis = System.currentTimeMillis();
        this.consumerRecord = consumerRecord;
        this.topicExpireTime = calculateTopicExpireTime();
        this.messageExpireTime = calculateMessageExpireTime();
    }

    private Long calculateTopicExpireTime() {
        Header header = this.consumerRecord.headers().lastHeader(EXPIRE_TIME_IN_TOPIC);
        return header != null ? Long.parseLong(new String(header.value(), UTF_8)) : null;
    }

    private Long calculateMessageExpireTime() {
        Header header = this.consumerRecord.headers().lastHeader(MESSAGE_EXPIRE_TIME);
        return header != null ? Long.parseLong(new String(header.value(), UTF_8)) : null;
    }

    public int getDelayTime() {
        return (int) ((messageExpireTime - millis) / 1000L);
    }


    public long getTopicExpireTime() {
        return (long) ObjectUtils.defaultIfNull(topicExpireTime, 0L);
    }

    public long getMessageExpireTime() {
        return messageExpireTime;
    }

    /**
     * 如果消息已经过期,或者在该级别的topic的延时时间已经被耗尽
     *
     * @return boolean
     */
    public boolean isExpired() {
        int delayTime = getDelayTime();
        return delayTime < 1 || topicExpireTime <= millis;
    }

    ProducerRecord<String, String> producerRecord() {
        String topic = consumerRecord.topic();
        String key = consumerRecord.key();
        String payload = consumerRecord.value();
        Headers headers = consumerRecord.headers();

        Objects.requireNonNull(messageExpireTime);
        headers.remove(EXPIRE_TIME_IN_TOPIC);
        int delay = (int) ((messageExpireTime - millis) / 1000L);
        Integer partition = null;
        //如果延时消息已经过期
        if (delay <= 0) {
            topic = new String(headers.lastHeader(ORIGIN_TOPIC).value(), UTF_8);
            headers.remove(MESSAGE_EXPIRE_TIME);
            headers.remove(ORIGIN_TOPIC);
            log.trace("Payload {} has expired, exceeded:{}s, send: {}", payload, delay, topic);
        } else {
            DelayLevel delayLevel = new DelayLevel(delay);
            partition = delayLevel.getPartition();
            int level = delayLevel.getLevel();
            long t = millis + level * 1000L;
            headers.add(EXPIRE_TIME_IN_TOPIC, String.valueOf(t).getBytes());
            log.trace("Payload {} has been reallocated, delay:{}s, send: {}-{}", payload, delay, topic, partition);
        }
        return new ProducerRecord<>(topic, partition, key, payload, headers);
    }
}
