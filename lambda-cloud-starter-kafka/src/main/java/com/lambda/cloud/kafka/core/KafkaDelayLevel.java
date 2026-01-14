package com.lambda.cloud.kafka.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

/**
 * 延时级别计算类
 * <p>
 * 根据延时时间计算对应的Kafka分区和延时级别。
 * 延时级别的计算规则：
 * <ul>
 * <li>延时时间小于10秒：分区和级别都等于延时时间</li>
 * <li>延时时间大于等于10秒：使用对数算法计算分区和级别</li>
 * </ul>
 *
 * 分区分配策略：
 * <ul>
 * <li>1-9秒：分区0-8，级别1-9</li>
 * <li>10-99秒：分区9，级别10</li>
 * <li>100-999秒：分区10，级别100</li>
 * <li>1000-9999秒：分区11，级别1000</li>
 * <li>10000-99999秒：分区12，级别10000</li>
 * <li>100000秒以上：分区13，级别100000</li>
 * </ul>
 *
 * @author jin
 * @since 1.0.0
 */
@Data
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class KafkaDelayLevel {

    /**
     * Kafka分区号
     */
    private int partition;

    /**
     * 延时级别（秒）
     */
    private int level;

    /**
     * 根据延时时间构造延时级别
     *
     * @param delay 延时时间（秒），必须大于0
     * @throws IllegalArgumentException 当延时时间小于等于0时抛出
     */
    @SuppressWarnings({"all"})
    public KafkaDelayLevel(int delay) {
        if (delay <= 0) {
            throw new IllegalArgumentException("Delay must be greater than 0, but was: " + delay);
        }

        if (delay < 10) {
            this.partition = delay - 1; // 分区从0开始，所以减1
            this.level = delay;
        } else {
            int i = Math.min((int) Math.log10(delay), 5);
            // 1-9秒占用分区0-8，10秒以上从分区9开始
            // log10(10)=1 -> 8+1=9
            // log10(100000)=5 -> 8+5=13
            this.partition = 8 + i;
            this.level = (int) Math.pow(10D, i);
        }
    }
}
