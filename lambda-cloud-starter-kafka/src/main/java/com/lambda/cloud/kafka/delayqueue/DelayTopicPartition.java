package com.lambda.cloud.kafka.delayqueue;

import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;

/**
 * @author jin
 */
@Slf4j
@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public class DelayTopicPartition {

    private final DelayQueue<DelayEntry> queue;
    private final Map<TopicPartition, OffsetAndMetadata> offsets;
    private final AtomicInteger counter;
    private final DelayOffsetCommitCallback delayOffsetCommitCallback = new DelayOffsetCommitCallback();

    private final int partition;

    public DelayTopicPartition(int partition) {
        this.partition = partition;
        this.queue = new DelayQueue<>();
        this.counter = new AtomicInteger(0);
        this.offsets = Maps.newHashMap();
    }

    public void seek(TopicPartition topicPartition, long offset) {
        if (!offsets.containsKey(topicPartition)) {
            offsets.put(topicPartition, new OffsetAndMetadata(offset + 1));
        } else {
            long position = offsets.get(topicPartition).offset();
            if (position < offset + 1) {
                offsets.put(topicPartition, new OffsetAndMetadata(offset + 1));
            }
        }
    }

    public void commit(KafkaConsumer<String, String> consumer) {
        if (MapUtils.isNotEmpty(offsets)) {
            consumer.commitAsync(offsets, delayOffsetCommitCallback);
            offsets.clear();
        }
    }

    public static class DelayOffsetCommitCallback implements OffsetCommitCallback {
        @Override
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            if (Objects.nonNull(exception)) {
                log.error(offsets.toString(), exception);
            }
        }
    }
}
