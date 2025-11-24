package com.lambda.cloud.kafka.core;

import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
     * 待处理的偏移量集合，用于追踪未完成的消息
     */
    private final ConcurrentSkipListSet<Long> pendingOffsets;

    /**
     * 最大已拉取的偏移量
     */
    private final AtomicLong maxFetchedOffset;

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
     * 队列最大容量限制
     */
    private static final int MAX_QUEUE_SIZE = 5000;

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
        this.pendingOffsets = new ConcurrentSkipListSet<>();
        this.maxFetchedOffset = new AtomicLong(-1);
    }

    /**
     * 添加待处理的偏移量
     *
     * @param offset 消息偏移量
     */
    public void addPendingOffset(long offset) {
        pendingOffsets.add(offset);
        maxFetchedOffset.accumulateAndGet(offset, Math::max);
    }

    /**
     * 移除已处理的偏移量
     *
     * @param offset 消息偏移量
     */
    public void removePendingOffset(long offset) {
        pendingOffsets.remove(offset);
    }

    /**
     * 检查队列是否已满
     *
     * @return true如果队列已满
     */
    public boolean isFull() {
        return queue.size() >= MAX_QUEUE_SIZE;
    }

    /**
     * 异步提交偏移量到Kafka
     * <p>
     * 计算可提交的安全偏移量（最小未处理偏移量），并异步提交。
     * 只有当所有小于N的消息都已处理时，才能提交偏移量N。
     *
     * @param consumer Kafka消费者实例
     * @throws IllegalArgumentException 当消费者为null时抛出
     */
    public void commit(KafkaConsumer<String, String> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("KafkaConsumer cannot be null");
        }

        long commitOffset;
        if (pendingOffsets.isEmpty()) {
            long max = maxFetchedOffset.get();
            if (max < 0) {
                return;
            }
            commitOffset = max + 1;
        } else {
            commitOffset = pendingOffsets.first();
        }

        Map<TopicPartition, OffsetAndMetadata> offsets = Maps.newHashMap();
        offsets.put(
                new TopicPartition(KafkaDelayRecord.DELAY_TOPIC, partition),
                new OffsetAndMetadata(commitOffset));

        consumer.commitAsync(offsets, delayOffsetCommitCallback);
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
