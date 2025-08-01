package com.lambda.cloud.dubbo.monitor;

import com.lambda.autoconfig.DubboProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * Dubbo性能指标收集器
 * <p>
 * 负责收集和统计Dubbo服务调用的各种性能指标，为监控、告警和性能分析提供数据支持。
 * 该组件是线程安全的，支持高并发环境下的指标收集。
 * </p>
 *
 * <p>收集的指标包括：</p>
 * <ul>
 *   <li><strong>请求统计</strong>: 总请求数、成功请求数、失败请求数、慢请求数</li>
 *   <li><strong>响应时间</strong>: 平均响应时间、最大响应时间、最小响应时间</li>
 *   <li><strong>成功率</strong>: 基于成功/失败请求计算的成功率百分比</li>
 *   <li><strong>异常统计</strong>: 按异常类型统计的错误计数</li>
 *   <li><strong>慢调用监控</strong>: 超过阈值的调用自动标记和告警</li>
 * </ul>
 *
 * <p>配置控制：</p>
 * <pre>
 * lambda:
 *   dubbo:
 *     monitoring:
 *       enable-metrics: true          # 是否启用指标收集
 *       slow-call-threshold: 1000     # 慢调用阈值(毫秒)
 *       metrics-prefix: "dubbo"       # 指标前缀
 * </pre>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 获取特定服务的指标
 * ServiceMetrics metrics = metricsCollector.getMetrics("UserService.getUserById");
 * System.out.println("成功率: " + metrics.getSuccessRate() + "%");
 * System.out.println("平均响应时间: " + metrics.getAverageDuration() + "ms");
 *
 * // 获取所有服务的指标
 * Map&lt;String, ServiceMetrics&gt; allMetrics = metricsCollector.getAllMetrics();
 * </pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboProperties.Monitoring
 * @see ServiceMetrics
 */
@Slf4j
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "Metrics collector requires configuration object from framework")
public class DubboMetricsCollector {

    private final DubboProperties.Monitoring monitoringProperties;

    /**
     * 服务指标存储映射
     * <p>Key: 服务标识(类名.方法名), Value: 对应的指标统计对象</p>
     */
    private final ConcurrentHashMap<String, ServiceMetrics> serviceMetricsMap = new ConcurrentHashMap<>();

    /**
     * 记录服务调用请求的性能指标
     * <p>
     * 该方法会根据调用结果更新相应的统计指标，包括请求计数、响应时间、
     * 成功/失败状态、慢调用检测和异常统计等。
     * </p>
     *
     * @param invoker RPC调用器，用于获取服务接口信息
     * @param invocation RPC调用信息，包含方法名等
     * @param duration 调用耗时(毫秒)
     * @param success 调用是否成功
     * @param error 调用异常(如果有的话)
     */
    public void recordRequest(
            Invoker<?> invoker, Invocation invocation, long duration, boolean success, Throwable error) {
        // 如果指标收集功能被禁用，直接返回
        if (!monitoringProperties.isEnableMetrics()) {
            return;
        }

        String serviceKey = getServiceKey(invoker, invocation);
        ServiceMetrics metrics = serviceMetricsMap.computeIfAbsent(serviceKey, k -> new ServiceMetrics());

        // 更新请求计数统计
        metrics.incrementTotalRequests();
        if (success) {
            metrics.incrementSuccessRequests();
        } else {
            metrics.incrementFailureRequests();
        }

        // 更新响应时间统计
        metrics.updateDuration(duration);

        // 慢调用检测和告警
        if (duration > monitoringProperties.getSlowCallThreshold()) {
            metrics.incrementSlowRequests();
            log.warn(
                    "检测到慢调用 - service: {}, duration: {}ms, threshold: {}ms",
                    serviceKey,
                    duration,
                    monitoringProperties.getSlowCallThreshold());
        }

        // 记录异常统计
        if (error != null) {
            String errorType = error.getClass().getSimpleName();
            metrics.incrementErrors(errorType);

            if (log.isDebugEnabled()) {
                log.debug(
                        "记录服务调用异常 - service: {}, errorType: {}, message: {}",
                        serviceKey,
                        errorType,
                        error.getMessage());
            }
        }

        // 记录指标更新的调试信息
        if (log.isTraceEnabled()) {
            log.trace(
                    "指标记录完成 - service: {}, duration: {}ms, success: {}, totalRequests: {}",
                    serviceKey,
                    duration,
                    success,
                    metrics.getTotalRequests());
        }
    }

    /**
     * 获取指定服务的性能指标
     *
     * @param serviceKey 服务标识，格式为"类名.方法名"
     * @return 服务性能指标对象，如果不存在则返回null
     */
    public ServiceMetrics getMetrics(String serviceKey) {
        return serviceMetricsMap.get(serviceKey);
    }

    /**
     * 获取所有服务的性能指标
     * <p>
     * 返回当前所有已收集指标的副本，避免并发修改问题。
     * </p>
     *
     * @return 包含所有服务指标的映射副本
     */
    public ConcurrentHashMap<String, ServiceMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(serviceMetricsMap);
    }

    /**
     * 清空所有指标数据
     * <p>
     * 用于重置指标统计，通常在需要重新开始统计时使用。
     * </p>
     */
    public void clearAllMetrics() {
        serviceMetricsMap.clear();
        log.info("所有Dubbo指标数据已清空");
    }

    /**
     * 生成服务标识Key
     * <p>
     * 根据调用器和调用信息生成唯一的服务标识，格式为"接口简名.方法名"。
     * </p>
     *
     * @param invoker RPC调用器
     * @param invocation RPC调用信息
     * @return 服务标识字符串
     */
    private String getServiceKey(Invoker<?> invoker, Invocation invocation) {
        return invoker.getInterface().getSimpleName() + "." + invocation.getMethodName();
    }

    /**
     * 服务性能指标统计类
     * <p>
     * 线程安全的指标统计容器，使用原子变量确保并发环境下的数据一致性。
     * 包含了服务调用的各种性能指标和统计方法。
     * </p>
     */
    public static class ServiceMetrics {

        /** 总请求数 */
        private final AtomicLong totalRequests = new AtomicLong(0);

        /** 成功请求数 */
        private final AtomicLong successRequests = new AtomicLong(0);

        /** 失败请求数 */
        private final AtomicLong failureRequests = new AtomicLong(0);

        /** 慢请求数 */
        private final AtomicLong slowRequests = new AtomicLong(0);

        /** 总响应时间(用于计算平均值) */
        private final AtomicLong totalDuration = new AtomicLong(0);

        /** 最大响应时间 */
        private final AtomicLong maxDuration = new AtomicLong(0);

        /** 最小响应时间 */
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);

        /** 按异常类型统计的错误计数 */
        private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

        /**
         * 增加总请求数
         */
        public void incrementTotalRequests() {
            totalRequests.incrementAndGet();
        }

        /**
         * 增加成功请求数
         */
        public void incrementSuccessRequests() {
            successRequests.incrementAndGet();
        }

        /**
         * 增加失败请求数
         */
        public void incrementFailureRequests() {
            failureRequests.incrementAndGet();
        }

        /**
         * 增加慢请求数
         */
        public void incrementSlowRequests() {
            slowRequests.incrementAndGet();
        }

        /**
         * 更新响应时间统计
         * <p>
         * 同时更新总响应时间、最大响应时间和最小响应时间。
         * </p>
         *
         * @param duration 本次调用的响应时间(毫秒)
         */
        public void updateDuration(long duration) {
            totalDuration.addAndGet(duration);
            maxDuration.updateAndGet(current -> Math.max(current, duration));
            minDuration.updateAndGet(current -> Math.min(current, duration));
        }

        /**
         * 增加指定类型的错误计数
         *
         * @param errorType 异常类型名称
         */
        public void incrementErrors(String errorType) {
            errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Getter方法

        /**
         * 获取总请求数
         * @return 总请求数
         */
        public long getTotalRequests() {
            return totalRequests.get();
        }

        /**
         * 获取成功请求数
         * @return 成功请求数
         */
        public long getSuccessRequests() {
            return successRequests.get();
        }

        /**
         * 获取失败请求数
         * @return 失败请求数
         */
        public long getFailureRequests() {
            return failureRequests.get();
        }

        /**
         * 获取慢请求数
         * @return 慢请求数
         */
        public long getSlowRequests() {
            return slowRequests.get();
        }

        /**
         * 获取平均响应时间
         * @return 平均响应时间(毫秒)，如果没有请求则返回0
         */
        public long getAverageDuration() {
            long total = totalRequests.get();
            return total > 0 ? totalDuration.get() / total : 0;
        }

        /**
         * 获取最大响应时间
         * @return 最大响应时间(毫秒)
         */
        public long getMaxDuration() {
            return maxDuration.get();
        }

        /**
         * 获取最小响应时间
         * @return 最小响应时间(毫秒)，如果没有请求则返回0
         */
        public long getMinDuration() {
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        /**
         * 获取成功率
         * @return 成功率百分比(0-100)，如果没有请求则返回0
         */
        public double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successRequests.get() / total * 100 : 0;
        }

        /**
         * 获取错误计数统计
         * @return 按异常类型分组的错误计数映射
         */
        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Error counts map is used for read-only monitoring purposes")
        public ConcurrentHashMap<String, AtomicLong> getErrorCounts() {
            return errorCounts;
        }
    }
}
