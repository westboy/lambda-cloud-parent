package com.lambda.cloud.test.assertion;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 扩展的时间断言类。
 */
@SuppressWarnings("all")
public record LambdaDateTimeAssert(LocalDateTime actual) {

    /**
     * 断言时间不为null。
     *
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isNotNull() {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        return this;
    }

    /**
     * 断言时间等于指定时间。
     *
     * @param expected 期望时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isEqualTo(LocalDateTime expected) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (expected == null) {
            throw new AssertionError("期望时间不能为 null");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError(String.format("时间 %s 不等于期望时间 %s", actual, expected));
        }
        return this;
    }

    /**
     * 断言时间在指定时间范围内是最近的。
     *
     * @param duration 时间范围
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isRecentWithin(Duration duration) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (duration == null) {
            throw new AssertionError("时间范围不能为 null");
        }
        if (duration.isNegative()) {
            throw new AssertionError("时间范围不能为负数");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minus(duration);
        if (actual.isBefore(threshold)) {
            throw new AssertionError(String.format("时间 %s 不在最近 %s 范围内", actual, duration));
        }
        return this;
    }

    /**
     * 断言时间在指定范围内。
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isBetween(LocalDateTime start, LocalDateTime end) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (start == null) {
            throw new AssertionError("开始时间不能为 null");
        }
        if (end == null) {
            throw new AssertionError("结束时间不能为 null");
        }
        if (start.isAfter(end)) {
            throw new AssertionError(String.format("开始时间 %s 不能晚于结束时间 %s", start, end));
        }
        if (actual.isBefore(start) || actual.isAfter(end)) {
            throw new AssertionError(String.format("时间 %s 不在范围 [%s, %s] 内", actual, start, end));
        }
        return this;
    }

    /**
     * 断言时间在指定时间之后。
     *
     * @param other 比较时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isAfter(LocalDateTime other) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (other == null) {
            throw new AssertionError("比较时间不能为 null");
        }
        if (!actual.isAfter(other)) {
            throw new AssertionError(String.format("时间 %s 不在 %s 之后", actual, other));
        }
        return this;
    }

    /**
     * 断言时间在指定时间之前。
     *
     * @param other 比较时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isBefore(LocalDateTime other) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (other == null) {
            throw new AssertionError("比较时间不能为 null");
        }
        if (!actual.isBefore(other)) {
            throw new AssertionError(String.format("时间 %s 不在 %s 之前", actual, other));
        }
        return this;
    }

    /**
     * 断言时间在另一个时间之前或等于。
     *
     * @param other 比较时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isBeforeOrEqualTo(LocalDateTime other) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (other == null) {
            throw new AssertionError("比较时间不能为 null");
        }
        if (actual.isAfter(other)) {
            throw new AssertionError(String.format("时间 %s 不在 %s 之前或等于", actual, other));
        }
        return this;
    }

    /**
     * 断言时间在另一个时间之后或等于。
     *
     * @param other 比较时间
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isAfterOrEqualTo(LocalDateTime other) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (other == null) {
            throw new AssertionError("比较时间不能为 null");
        }
        if (actual.isBefore(other)) {
            throw new AssertionError(String.format("时间 %s 不在 %s 之后或等于", actual, other));
        }
        return this;
    }

    /**
     * 断言时间是今天。
     *
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isToday() {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        LocalDate today = LocalDate.now();
        if (!actual.toLocalDate().equals(today)) {
            throw new AssertionError(String.format("时间 %s 不是今天 %s", actual, today));
        }
        return this;
    }

    /**
     * 断言时间与指定时间的差值在容忍范围内。
     *
     * @param expected  期望时间
     * @param tolerance 容忍时间差（秒）
     * @return 当前断言实例
     */
    public LambdaDateTimeAssert isCloseTo(LocalDateTime expected, long tolerance) {
        if (actual == null) {
            throw new AssertionError("时间为 null");
        }
        if (expected == null) {
            throw new AssertionError("期望时间不能为 null");
        }
        if (tolerance < 0) {
            throw new AssertionError("容忍时间差不能为负数");
        }

        long diffSeconds = Math.abs(Duration.between(actual, expected).getSeconds());
        if (diffSeconds > tolerance) {
            throw new AssertionError(
                    String.format("时间 %s 与期望时间 %s 的差值 %d 秒超过容忍范围 %d 秒", actual, expected, diffSeconds, tolerance));
        }
        return this;
    }
}
