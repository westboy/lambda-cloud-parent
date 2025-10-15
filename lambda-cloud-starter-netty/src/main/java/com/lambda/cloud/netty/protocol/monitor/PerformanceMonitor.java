package com.lambda.cloud.netty.protocol.monitor;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能监控器
 * <p>
 * 用于监控协议引擎的性能指标，包括解析时间、序列化时间、错误率等
 * </p>
 *
 * @author Jin
 */
public class PerformanceMonitor {

    /**
     * 解析操作计数
     */
    private final LongAdder parseCount = new LongAdder();

    /**
     * 序列化操作计数
     */
    private final LongAdder serializeCount = new LongAdder();

    /**
     * 验证操作计数
     */
    private final LongAdder validateCount = new LongAdder();

    /**
     * 解析总时间（纳秒）
     */
    private final LongAdder parseTotalTime = new LongAdder();

    /**
     * 序列化总时间（纳秒）
     */
    private final LongAdder serializeTotalTime = new LongAdder();

    /**
     * 验证总时间（纳秒）
     */
    private final LongAdder validateTotalTime = new LongAdder();

    /**
     * 解析错误计数
     */
    private final LongAdder parseErrorCount = new LongAdder();

    /**
     * 序列化错误计数
     */
    private final LongAdder serializeErrorCount = new LongAdder();

    /**
     * 验证错误计数
     */
    private final LongAdder validateErrorCount = new LongAdder();

    /**
     * 最大解析时间（纳秒）
     */
    private final AtomicLong maxParseTime = new AtomicLong(0);

    /**
     * 最大序列化时间（纳秒）
     */
    private final AtomicLong maxSerializeTime = new AtomicLong(0);

    /**
     * 最大验证时间（纳秒）
     */
    private final AtomicLong maxValidateTime = new AtomicLong(0);

    /**
     * 记录解析操作
     *
     * @param duration 执行时间（纳秒）
     * @param success  是否成功
     */
    public void recordParse(long duration, boolean success) {
        parseCount.increment();
        parseTotalTime.add(duration);
        updateMaxTime(maxParseTime, duration);

        if (!success) {
            parseErrorCount.increment();
        }
    }

    /**
     * 记录序列化操作
     *
     * @param duration 执行时间（纳秒）
     * @param success  是否成功
     */
    public void recordSerialize(long duration, boolean success) {
        serializeCount.increment();
        serializeTotalTime.add(duration);
        updateMaxTime(maxSerializeTime, duration);

        if (!success) {
            serializeErrorCount.increment();
        }
    }

    /**
     * 记录验证操作
     *
     * @param duration 执行时间（纳秒）
     * @param success  是否成功
     */
    public void recordValidate(long duration, boolean success) {
        validateCount.increment();
        validateTotalTime.add(duration);
        updateMaxTime(maxValidateTime, duration);

        if (!success) {
            validateErrorCount.increment();
        }
    }

    /**
     * 获取性能统计信息
     *
     * @return 性能统计
     */
    public PerformanceStats getStats() {
        return new PerformanceStats(
                parseCount.sum(),
                serializeCount.sum(),
                validateCount.sum(),
                calculateAverage(parseTotalTime.sum(), parseCount.sum()),
                calculateAverage(serializeTotalTime.sum(), serializeCount.sum()),
                calculateAverage(validateTotalTime.sum(), validateCount.sum()),
                maxParseTime.get(),
                maxSerializeTime.get(),
                maxValidateTime.get(),
                calculateErrorRate(parseErrorCount.sum(), parseCount.sum()),
                calculateErrorRate(serializeErrorCount.sum(), serializeCount.sum()),
                calculateErrorRate(validateErrorCount.sum(), validateCount.sum()));
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        parseCount.reset();
        serializeCount.reset();
        validateCount.reset();
        parseTotalTime.reset();
        serializeTotalTime.reset();
        validateTotalTime.reset();
        parseErrorCount.reset();
        serializeErrorCount.reset();
        validateErrorCount.reset();
        maxParseTime.set(0);
        maxSerializeTime.set(0);
        maxValidateTime.set(0);
    }

    /**
     * 更新最大时间
     */
    private void updateMaxTime(AtomicLong maxTime, long duration) {
        long current = maxTime.get();
        while (duration > current && !maxTime.compareAndSet(current, duration)) {
            current = maxTime.get();
        }
    }

    /**
     * 计算平均值
     */
    private double calculateAverage(long total, long count) {
        return count == 0 ? 0.0 : (double) total / count;
    }

    /**
     * 计算错误率
     */
    private double calculateErrorRate(long errors, long total) {
        return total == 0 ? 0.0 : (double) errors / total;
    }

    /**
     * 性能统计信息
     */
    public record PerformanceStats(
            long parseCount,
            long serializeCount,
            long validateCount,
            double avgParseTime,
            double avgSerializeTime,
            double avgValidateTime,
            long maxParseTime,
            long maxSerializeTime,
            long maxValidateTime,
            double parseErrorRate,
            double serializeErrorRate,
            double validateErrorRate) {
        @Override
        public String toString() {
            return String.format(
                    "PerformanceStats{\n" + "  Parse: count=%d, avgTime=%.2fns, maxTime=%dns, errorRate=%.2f%%\n"
                            + "  Serialize: count=%d, avgTime=%.2fns, maxTime=%dns, errorRate=%.2f%%\n"
                            + "  Validate: count=%d, avgTime=%.2fns, maxTime=%dns, errorRate=%.2f%%\n"
                            + "}",
                    parseCount,
                    avgParseTime,
                    maxParseTime,
                    parseErrorRate * 100,
                    serializeCount,
                    avgSerializeTime,
                    maxSerializeTime,
                    serializeErrorRate * 100,
                    validateCount,
                    avgValidateTime,
                    maxValidateTime,
                    validateErrorRate * 100);
        }
    }
}
