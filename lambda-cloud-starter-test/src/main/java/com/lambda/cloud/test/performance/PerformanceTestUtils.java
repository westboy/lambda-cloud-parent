package com.lambda.cloud.test.performance;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 性能测试工具类。
 * <p>
 * 该工具类提供性能测试和基准测试的便捷功能，包括执行时间测量、
 * 并发性能测试、内存使用监控、吞吐量测试等。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>方法执行时间测量</li>
 *     <li>并发性能测试</li>
 *     <li>内存使用监控</li>
 *     <li>吞吐量和延迟测试</li>
 *     <li>性能基准比较</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 测量方法执行时间
 * Duration duration = PerformanceTestUtils.measureTime(() -> {
 *     // 待测试的代码
 *     service.processData();
 * });
 *
 * // 并发性能测试
 * ConcurrentTestResult result = PerformanceTestUtils.concurrentTest(
 *     () -> service.processRequest(), 100, 10);
 *
 * // 吞吐量测试
 * ThroughputTestResult throughput = PerformanceTestUtils.throughputTest(
 *     () -> service.handleRequest(), Duration.ofSeconds(10));
 * </pre>
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public class PerformanceTestUtils {

    private static final int DEFAULT_WARMUP_ITERATIONS = 5;
    private static final int DEFAULT_MEASUREMENT_ITERATIONS = 10;

    /**
     * 测量方法执行时间。
     *
     * @param operation 待测试的操作
     * @return 执行时间
     */
    public static Duration measureTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return Duration.ofNanos(endTime - startTime);
    }

    /**
     * 测量方法执行时间（带返回值）。
     *
     * @param operation 待测试的操作
     * @param <T>       返回值类型
     * @return 测量结果
     */
    public static <T> TimedResult<T> measureTimeWithResult(Supplier<T> operation) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        Duration duration = Duration.ofNanos(endTime - startTime);
        return new TimedResult<>(result, duration);
    }

    /**
     * 基准测试（多次执行取平均值）。
     *
     * @param operation           待测试的操作
     * @param warmupIterations    预热次数
     * @param measurementIterations 测量次数
     * @return 基准测试结果
     */
    public static BenchmarkResult benchmark(Runnable operation, int warmupIterations, int measurementIterations) {
        log.info("开始基准测试，预热次数: {}, 测量次数: {}", warmupIterations, measurementIterations);

        // 预热
        for (int i = 0; i < warmupIterations; i++) {
            operation.run();
        }

        // 测量
        List<Duration> measurements = new ArrayList<>();
        for (int i = 0; i < measurementIterations; i++) {
            Duration duration = measureTime(operation);
            measurements.add(duration);
        }

        return calculateBenchmarkResult(measurements);
    }

    /**
     * 基准测试（使用默认参数）。
     *
     * @param operation 待测试的操作
     * @return 基准测试结果
     */
    public static BenchmarkResult benchmark(Runnable operation) {
        return benchmark(operation, DEFAULT_WARMUP_ITERATIONS, DEFAULT_MEASUREMENT_ITERATIONS);
    }

    /**
     * 并发性能测试。
     *
     * @param operation     待测试的操作
     * @param threadCount   线程数
     * @param iterationsPerThread 每个线程的执行次数
     * @return 并发测试结果
     */
    public static ConcurrentTestResult concurrentTest(Runnable operation, int threadCount, int iterationsPerThread) {
        log.info("开始并发性能测试，线程数: {}, 每线程执行次数: {}", threadCount, iterationsPerThread);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Future<List<Duration>>> futures = new ArrayList<>();

        // 提交任务
        for (int i = 0; i < threadCount; i++) {
            Future<List<Duration>> future = executor.submit(() -> {
                List<Duration> threadMeasurements = new ArrayList<>();
                try {
                    startLatch.await(); // 等待统一开始

                    for (int j = 0; j < iterationsPerThread; j++) {
                        Duration duration = measureTime(operation);
                        threadMeasurements.add(duration);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("线程被中断", e);
                } finally {
                    endLatch.countDown();
                }
                return threadMeasurements;
            });
            futures.add(future);
        }

        // 开始测试
        long startTime = System.nanoTime();
        startLatch.countDown();

        try {
            endLatch.await(); // 等待所有线程完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待线程完成时被中断", e);
        }

        long endTime = System.nanoTime();
        Duration totalTime = Duration.ofNanos(endTime - startTime);

        // 收集结果
        List<Duration> allMeasurements = new ArrayList<>();
        for (Future<List<Duration>> future : futures) {
            try {
                allMeasurements.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("获取线程执行结果失败", e);
            }
        }

        executor.shutdown();

        return new ConcurrentTestResult(threadCount, iterationsPerThread, totalTime, allMeasurements);
    }

    /**
     * 吞吐量测试。
     *
     * @param operation 待测试的操作
     * @param duration  测试持续时间
     * @return 吞吐量测试结果
     */
    public static ThroughputTestResult throughputTest(Runnable operation, Duration duration) {
        log.info("开始吞吐量测试，持续时间: {}", duration);

        long endTime = System.nanoTime() + duration.toNanos();
        int executionCount = 0;
        List<Duration> executionTimes = new ArrayList<>();

        while (System.nanoTime() < endTime) {
            Duration executionTime = measureTime(operation);
            executionTimes.add(executionTime);
            executionCount++;
        }

        double throughput = (double) executionCount / duration.toSeconds();

        return new ThroughputTestResult(executionCount, duration, throughput, executionTimes);
    }

    /**
     * 内存使用测试。
     *
     * @param operation 待测试的操作
     * @return 内存使用结果
     */
    @SuppressFBWarnings("DM_GC")
    public static MemoryUsageResult measureMemoryUsage(Runnable operation) {
        Runtime runtime = Runtime.getRuntime();

        // 强制垃圾回收
        System.gc();
        Thread.yield();

        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        operation.run();

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;

        return new MemoryUsageResult(beforeMemory, afterMemory, memoryUsed);
    }

    /**
     * 计算基准测试结果。
     *
     * @param measurements 测量结果列表
     * @return 基准测试结果
     */
    private static BenchmarkResult calculateBenchmarkResult(List<Duration> measurements) {
        if (measurements.isEmpty()) {
            throw new IllegalArgumentException("测量结果不能为空");
        }

        // 计算平均值
        long totalNanos = measurements.stream().mapToLong(Duration::toNanos).sum();
        Duration average = Duration.ofNanos(totalNanos / measurements.size());

        // 计算最小值和最大值
        Duration min = measurements.stream().min(Duration::compareTo).orElse(Duration.ZERO);
        Duration max = measurements.stream().max(Duration::compareTo).orElse(Duration.ZERO);

        // 计算标准差
        double avgNanos = average.toNanos();
        double variance = measurements.stream()
                .mapToDouble(d -> Math.pow(d.toNanos() - avgNanos, 2))
                .average()
                .orElse(0.0);
        Duration standardDeviation = Duration.ofNanos((long) Math.sqrt(variance));

        return new BenchmarkResult(average, min, max, standardDeviation, measurements.size());
    }

    /**
     * 计时结果类。
     *
     * @param <T> 结果类型
     */
    @Data
    public static class TimedResult<T> {
        private final T result;
        private final Duration duration;
    }

    /**
     * 基准测试结果类。
     */
    @Data
    public static class BenchmarkResult {
        private final Duration average;
        private final Duration min;
        private final Duration max;
        private final Duration standardDeviation;
        private final int sampleCount;

        /**
         * 获取每秒操作数。
         *
         * @return 每秒操作数
         */
        public double getOperationsPerSecond() {
            return 1_000_000_000.0 / average.toNanos();
        }

        @Override
        public String toString() {
            return String.format(
                    "BenchmarkResult{平均: %s, 最小: %s, 最大: %s, 标准差: %s, 样本数: %d, 每秒操作数: %.2f}",
                    average, min, max, standardDeviation, sampleCount, getOperationsPerSecond());
        }
    }

    /**
     * 并发测试结果类。
     */
    @Data
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ConcurrentTestResult {
        private final int threadCount;
        private final int iterationsPerThread;
        private final Duration totalTime;
        private final List<Duration> allMeasurements;

        /**
         * 获取总操作数。
         *
         * @return 总操作数
         */
        public int getTotalOperations() {
            return threadCount * iterationsPerThread;
        }

        /**
         * 获取平均执行时间。
         *
         * @return 平均执行时间
         */
        public Duration getAverageExecutionTime() {
            long totalNanos =
                    allMeasurements.stream().mapToLong(Duration::toNanos).sum();
            return Duration.ofNanos(totalNanos / allMeasurements.size());
        }

        /**
         * 获取吞吐量。
         *
         * @return 每秒操作数
         */
        public double getThroughput() {
            return (double) getTotalOperations() / totalTime.toSeconds();
        }

        @Override
        public String toString() {
            return String.format(
                    "ConcurrentTestResult{线程数: %d, 每线程操作数: %d, 总时间: %s, 平均执行时间: %s, 吞吐量: %.2f ops/s}",
                    threadCount, iterationsPerThread, totalTime, getAverageExecutionTime(), getThroughput());
        }
    }

    /**
     * 吞吐量测试结果类。
     */
    @Data
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ThroughputTestResult {
        private final int executionCount;
        private final Duration testDuration;
        private final double throughput;
        private final List<Duration> executionTimes;

        /**
         * 获取平均执行时间。
         *
         * @return 平均执行时间
         */
        public Duration getAverageExecutionTime() {
            long totalNanos =
                    executionTimes.stream().mapToLong(Duration::toNanos).sum();
            return Duration.ofNanos(totalNanos / executionTimes.size());
        }

        @Override
        public String toString() {
            return String.format(
                    "ThroughputTestResult{执行次数: %d, 测试时长: %s, 吞吐量: %.2f ops/s, 平均执行时间: %s}",
                    executionCount, testDuration, throughput, getAverageExecutionTime());
        }
    }

    /**
     * 内存使用结果类。
     */
    @Data
    public static class MemoryUsageResult {
        private final long beforeMemory;
        private final long afterMemory;
        private final long memoryUsed;

        /**
         * 获取内存使用量（MB）。
         *
         * @return 内存使用量（MB）
         */
        public double getMemoryUsedMB() {
            return memoryUsed / (1024.0 * 1024.0);
        }

        @Override
        public String toString() {
            return String.format(
                    "MemoryUsageResult{使用前: %d bytes, 使用后: %d bytes, 使用量: %d bytes (%.2f MB)}",
                    beforeMemory, afterMemory, memoryUsed, getMemoryUsedMB());
        }
    }
}
