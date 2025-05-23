package com.lambda.cloud.kafka.core;

import lombok.Data;

/**
 * DelayLevel
 *
 * @author jin
 */
@Data
public class DelayLevel {

    private int partition;

    private int level;

    @SuppressWarnings({"all", "pmd"})
    public DelayLevel(int delay) {
        if (delay < 10) {
            this.partition = delay;
            this.level = delay;
        } else {
            int i = Math.min((int) Math.log10(delay), 5);
            // 分区从0开始
            this.partition = 9 + i;
            this.level = (int) Math.pow(10D, i);
        }
    }
}
