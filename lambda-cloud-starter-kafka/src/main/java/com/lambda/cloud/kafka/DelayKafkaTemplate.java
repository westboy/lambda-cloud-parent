package com.lambda.cloud.kafka;

import static com.lambda.cloud.kafka.core.DelayConsumerRecord.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.lambda.cloud.kafka.core.DelayLevel;
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
 * DelayKafkaTemplate
 *
 * @author Jin
 */
@Slf4j
@Validated
public class DelayKafkaTemplate {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public DelayKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, String>> send(
            @NotBlank String topic, @NotBlank String payload, @Min(value = 1, message = "最小延迟时长1(秒)") int delay) {
        long millis = System.currentTimeMillis();
        DelayLevel delayLevel = new DelayLevel(delay);
        int partition = delayLevel.getPartition();
        int level = delayLevel.getLevel();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(DELAY_TOPIC, partition, null, payload);
        Headers headers = producerRecord.headers();
        headers.add(ORIGIN_TOPIC, topic.getBytes(UTF_8));
        long t = millis + level * 1000L;
        long m = millis + delay * 1000L;
        headers.add(EXPIRE_TIME_IN_TOPIC, String.valueOf(t).getBytes(UTF_8));
        headers.add(MESSAGE_EXPIRE_TIME, String.valueOf(m).getBytes(UTF_8));
        log.debug(
                "payload: {}, delay: {}s, send: {}-{}",
                payload,
                delay,
                producerRecord.topic(),
                producerRecord.partition());
        return send(producerRecord);
    }

    public CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> producerRecord) {
        return kafkaTemplate.send(producerRecord);
    }
}
