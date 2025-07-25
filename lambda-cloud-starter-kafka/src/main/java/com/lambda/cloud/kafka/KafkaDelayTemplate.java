package com.lambda.cloud.kafka;

import static com.lambda.cloud.kafka.core.KafkaDelayRecord.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.lambda.cloud.kafka.core.KafkaDelayLevel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.validation.annotation.Validated;

/**
 * 延时Kafka模板类
 * <p>
 * 提供延时消息发送功能，支持指定延时时间发送消息到目标主题。
 * 延时消息会先发送到延时队列，在指定时间后再转发到目标主题。
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
@Validated
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
public class KafkaDelayTemplate {

    /**
     * 底层Kafka模板
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 构造函数
     *
     * @param kafkaTemplate Kafka模板实例
     */
    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW")
    public KafkaDelayTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        if (kafkaTemplate == null) {
            throw new IllegalArgumentException("KafkaTemplate cannot be null");
        }
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送延时消息
     *
     * @param topic   目标主题
     * @param payload 消息内容
     * @param delay   延时时间（秒）
     * @return 发送结果的CompletableFuture
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public CompletableFuture<SendResult<String, String>> send(
            @NotBlank String topic, @NotBlank String payload, @Min(value = 1, message = "最小延迟时长1(秒)") int delay) {

        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (delay < 1) {
            throw new IllegalArgumentException("Delay must be at least 1 second");
        }

        long currentTimeMillis = System.currentTimeMillis();
        KafkaDelayLevel kafkaDelayLevel = new KafkaDelayLevel(delay);
        int partition = kafkaDelayLevel.getPartition();
        int level = kafkaDelayLevel.getLevel();

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(DELAY_TOPIC, partition, null, payload);
        Headers headers = producerRecord.headers();

        // 设置原始主题
        headers.add(ORIGIN_TOPIC, topic.getBytes(UTF_8));

        // 设置在当前延时级别的过期时间
        long topicExpireTime = currentTimeMillis + level * 1000L;
        headers.add(EXPIRE_TIME_IN_TOPIC, String.valueOf(topicExpireTime).getBytes(UTF_8));

        // 设置消息最终过期时间
        long messageExpireTime = currentTimeMillis + delay * 1000L;
        headers.add(MESSAGE_EXPIRE_TIME, String.valueOf(messageExpireTime).getBytes(UTF_8));

        log.debug(
                "Sending delay message - payload: {}, delay: {}s, topic: {}, partition: {}",
                payload,
                delay,
                producerRecord.topic(),
                producerRecord.partition());

        return send(producerRecord);
    }

    /**
     * 发送生产者记录
     *
     * @param producerRecord 生产者记录
     * @return 发送结果的CompletableFuture
     */
    public CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> producerRecord) {
        if (producerRecord == null) {
            throw new IllegalArgumentException("ProducerRecord cannot be null");
        }
        return kafkaTemplate.send(producerRecord);
    }
}
