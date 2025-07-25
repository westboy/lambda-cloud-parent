package com.lambda.cloud.kafka.core;

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
 * 延时主题分区管理器
 * <p>
 * 管理特定分区的延时消息队列和偏移量提交。
 * 每个实例对应一个Kafka分区，维护该分区的延时队列和偏移量信息。
 *
 * 主要功能：
 * <ul>
 * <li>维护延时消息的优先级队列</li>
 * <li>管理消费者偏移量</li>
 * <li>异步提交偏移量到Kafka</li>
 * <li>统计队列中的消息数量</li>
 * </ul>
 *
 * @author jin
 * @since 1.0.0
 */
@Slf4j
@Getter
@SuppressFBWarnings("EI_EXPOSE_REP")
public class KafkaDelayPartition {

    /**
     * 延时消息队列，按照到期时间排序
     */
    private final DelayQueue<KafkaDelayEntry> queue;

    /**
     * 偏移量映射，用于跟踪各个主题分区的消费进度
     */
    private final Map<TopicPartition, OffsetAndMetadata> offsets;

    /**
     * 队列中消息数量计数器
     */
    private final AtomicInteger counter;

    /**
     * 偏移量提交回调处理器
     */
    private final DelayOffsetCommitCallback delayOffsetCommitCallback = new DelayOffsetCommitCallback();

    /**
     * 分区号
     */
    private final int partition;

    /**
     * 构造延时主题分区管理器
     *
     * @param partition 分区号，必须大于等于0
     * @throws IllegalArgumentException 当分区号小于0时抛出
     */
    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public KafkaDelayPartition(int partition) {
        if (partition < 0) {
            throw new IllegalArgumentException("Partition must be non-negative, but was: " + partition);
        }
        this.partition = partition;
        this.queue = new DelayQueue<>();
        this.counter = new AtomicInteger(0);
        this.offsets = Maps.newHashMap();
    }

    /**
     * 更新主题分区的偏移量
     * <p>
     * 记录消费者处理到的最新偏移量，用于后续的偏移量提交。
     * 如果当前记录的偏移量小于新偏移量，则更新为新偏移量。
     *
     * @param topicPartition 主题分区
     * @param offset         消息偏移量
     * @throws IllegalArgumentException 当参数为null或偏移量为负数时抛出
     */
    public void seek(TopicPartition topicPartition, long offset) {
        if (topicPartition == null) {
            throw new IllegalArgumentException("TopicPartition cannot be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative: " + offset);
        }

        if (!offsets.containsKey(topicPartition)) {
            offsets.put(topicPartition, new OffsetAndMetadata(offset + 1));
        } else {
            long position = offsets.get(topicPartition).offset();
            if (position < offset + 1) {
                offsets.put(topicPartition, new OffsetAndMetadata(offset + 1));
            }
        }
    }

    /**
     * 异步提交偏移量到Kafka
     * <p>
     * 将当前记录的所有偏移量异步提交到Kafka，提交完成后清空偏移量映射。
     * 如果没有待提交的偏移量，则不执行任何操作。
     *
     * @param consumer Kafka消费者实例
     * @throws IllegalArgumentException 当消费者为null时抛出
     */
    public void commit(KafkaConsumer<String, String> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("KafkaConsumer cannot be null");
        }

        if (MapUtils.isNotEmpty(offsets)) {
            consumer.commitAsync(offsets, delayOffsetCommitCallback);
            offsets.clear();
        }
    }

    /**
     * 延时偏移量提交回调处理器
     * <p>
     * 处理异步偏移量提交的结果，如果提交失败则记录错误日志。
     */
    public static class DelayOffsetCommitCallback implements OffsetCommitCallback {

        /**
         * 偏移量提交完成回调
         *
         * @param offsets   已提交的偏移量映射
         * @param exception 提交过程中的异常，如果为null表示提交成功
         */
        @Override
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            if (Objects.nonNull(exception)) {
                log.error("Failed to commit offsets: {}", offsets, exception);
            }
        }
    }
}
