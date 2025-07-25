package com.lambda.cloud.kafka.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

/**
 * 延时队列条目
 * <p>
 * 实现了{@link Delayed}接口，用于在延时队列中存储Kafka消费者记录。
 * 支持按照到期时间、分区和偏移量进行排序。
 *
 * 排序规则：
 * <ol>
 * <li>首先按照剩余延时时间排序</li>
 * <li>如果延时时间相同，按照分区号排序</li>
 * <li>如果分区号也相同，按照偏移量排序</li>
 * </ol>
 *
 * @author jin
 * @since 1.0.0
 */
@Data
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class KafkaDelayEntry implements Delayed {
    /**
     * 到期时间（毫秒时间戳）
     */
    private long activeTime;

    /**
     * Kafka消费者记录
     */
    private ConsumerRecord<String, String> consumerRecord;

    /**
     * 主题分区信息
     */
    private TopicPartition topicPartition;

    /**
     * 消息偏移量
     */
    private long offset;

    /**
     * 构造延时队列条目
     *
     * @param activeTime      到期时间（毫秒时间戳）
     * @param consumerRecord  Kafka消费者记录
     * @param partition       主题分区信息
     * @param offset          消息偏移量
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public KafkaDelayEntry(
            long activeTime, ConsumerRecord<String, String> consumerRecord, TopicPartition partition, long offset) {
        if (consumerRecord == null) {
            throw new IllegalArgumentException("ConsumerRecord cannot be null");
        }
        if (partition == null) {
            throw new IllegalArgumentException("TopicPartition cannot be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative: " + offset);
        }

        this.activeTime = activeTime;
        this.consumerRecord = consumerRecord;
        this.topicPartition = partition;
        this.offset = offset;
    }

    /**
     * 比较两个延时队列条目的优先级
     * <p>
     * 比较规则：
     * <ol>
     * <li>首先比较剩余延时时间，时间越短优先级越高</li>
     * <li>如果延时时间相同，比较分区号，分区号越小优先级越高</li>
     * <li>如果分区号也相同，比较偏移量，偏移量越小优先级越高</li>
     * </ol>
     *
     * @param delayed 要比较的延时对象
     * @return 负数表示当前对象优先级更高，正数表示参数对象优先级更高，0表示优先级相同
     * @throws ClassCastException 当参数不是DelayEntry类型时抛出
     */
    @Override
    public int compareTo(@Nonnull Delayed delayed) {
        KafkaDelayEntry that = (KafkaDelayEntry) delayed;
        long d = getDelay(TimeUnit.MILLISECONDS) - that.getDelay(TimeUnit.MILLISECONDS);
        if (d != 0) {
            return (d > 0) ? 1 : -1;
        } else if (this.topicPartition.partition() != that.topicPartition.partition()) {
            return (this.topicPartition.partition() - that.topicPartition.partition() > 0) ? 1 : -1;
        }
        return Long.compare(this.offset, that.offset);
    }

    /**
     * 获取剩余延时时间
     * <p>
     * 计算当前时间到到期时间的剩余时间。
     * 如果返回值小于等于0，表示已经到期。
     *
     * @param unit 时间单位
     * @return 剩余延时时间，负数或0表示已到期
     */
    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        return unit.convert(this.activeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}
