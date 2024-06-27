package com.jingfang.cloud.kafka.delayqueue;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import javax.annotation.Nonnull;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author jin
 */
@Data
public class DelayEntry implements Delayed {
    /**
     * 到期时间，单位毫秒
     */
    private long activeTime;
    private ConsumerRecord<String, String> consumerRecord;
    private TopicPartition topicPartition;
    private long offset;

    public DelayEntry(long activeTime, ConsumerRecord<String, String> consumerRecord, TopicPartition partition, long offset) {
        super();
        //将传入的时长转换为超时的时刻
        this.activeTime = activeTime;
        this.consumerRecord = consumerRecord;
        this.topicPartition = partition;
        this.offset = offset;
    }

    @Override
    public int compareTo(@Nonnull Delayed delayed) {
        DelayEntry that = (DelayEntry) delayed;
        long d = getDelay(TimeUnit.MILLISECONDS) - that.getDelay(TimeUnit.MILLISECONDS);
        if (d != 0) {
            return (d > 0) ? 1 : -1;
        } else if (this.topicPartition.partition() != that.topicPartition.partition()) {
            return (this.topicPartition.partition() - that.topicPartition.partition() > 0) ? 1 : -1;
        }
        return Long.compare(this.offset, that.offset);
    }

    /**
     * 返回元素的剩余时间
     *
     * @param unit
     * @return long
     */

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        return this.activeTime - System.currentTimeMillis();
    }
}