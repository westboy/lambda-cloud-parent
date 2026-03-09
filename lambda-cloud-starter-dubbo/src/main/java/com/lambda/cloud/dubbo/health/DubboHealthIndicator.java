package com.lambda.cloud.dubbo.health;

import com.lambda.cloud.dubbo.monitor.DubboMetricsCollector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * Dubbo健康检查指示器
 * <p>
 * 集成Spring Boot Actuator健康检查，基于服务调用指标评估Dubbo服务的健康状态。
 * 通过分析成功率、请求量和平均响应时间等指标来判断服务是否健康。
 * </p>
 *
 * <p>健康评估规则：</p>
 * <ul>
 *   <li>成功率低于95%且请求数大于10的服务被标记为不健康</li>
 *   <li>如果存在不健康的服务，整体状态为DOWN</li>
 *   <li>如果没有注册的服务，状态为UP但会显示相应提示</li>
 *   <li>所有服务健康时，整体状态为UP</li>
 * </ul>
 *
 * <p>健康检查端点：GET /actuator/health</p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboMetricsCollector
 */
@ConditionalOnClass(HealthIndicator.class)
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Health indicator requires metrics collector from framework")
public class DubboHealthIndicator implements HealthIndicator {

    private final DubboMetricsCollector metricsCollector;

    /**
     * 执行健康检查
     * <p>
     * 遍历所有已注册的Dubbo服务，检查其性能指标并评估健康状态。
     * 返回的健康信息包含每个服务的详细状态和统计数据。
     * </p>
     *
     * @return 健康检查结果，包含整体状态和各服务详细信息
     */
    @Override
    public Health health() {
        try {
            Map<String, DubboMetricsCollector.ServiceMetrics> allMetrics = metricsCollector.getAllMetrics();

            // 没有服务注册的情况
            if (allMetrics.isEmpty()) {
                return Health.up()
                        .withDetail("status", "No services registered")
                        .withDetail("message", "Dubbo services are ready to accept requests")
                        .build();
            }

            Health.Builder builder = Health.up();
            boolean hasUnhealthyServices = false;

            for (Map.Entry<String, DubboMetricsCollector.ServiceMetrics> entry : allMetrics.entrySet()) {
                String serviceName = entry.getKey();
                DubboMetricsCollector.ServiceMetrics metrics = entry.getValue();

                double successRate = metrics.getSuccessRate();
                long totalRequests = metrics.getTotalRequests();

                // 评估服务健康状态：成功率低于95%且有足够的请求样本时标记为不健康
                if (successRate < 95.0 && totalRequests > 10) {
                    hasUnhealthyServices = true;
                    builder.withDetail(serviceName + ".status", "UNHEALTHY")
                            .withDetail(
                                    serviceName + ".reason",
                                    "Low success rate: " + String.format("%.2f%%", successRate))
                            .withDetail(serviceName + ".successRate", String.format("%.2f%%", successRate));
                } else {
                    builder.withDetail(serviceName + ".status", "HEALTHY")
                            .withDetail(serviceName + ".successRate", String.format("%.2f%%", successRate));
                }

                // 添加详细的性能指标
                builder.withDetail(serviceName + ".totalRequests", totalRequests)
                        .withDetail(serviceName + ".successRequests", metrics.getSuccessRequests())
                        .withDetail(serviceName + ".failureRequests", metrics.getFailureRequests())
                        .withDetail(serviceName + ".averageDuration", metrics.getAverageDuration() + "ms")
                        .withDetail(serviceName + ".maxDuration", metrics.getMaxDuration() + "ms")
                        .withDetail(serviceName + ".slowRequests", metrics.getSlowRequests());

                // 如果有错误统计，添加错误详情
                if (!metrics.getErrorCounts().isEmpty()) {
                    Map<String, Long> errorDetails = new java.util.HashMap<>();
                    metrics.getErrorCounts().forEach((errorType, count) -> errorDetails.put(errorType, count.get()));
                    builder.withDetail(serviceName + ".errorTypes", errorDetails);
                }
            }

            // 设置整体健康状态
            if (hasUnhealthyServices) {
                return builder.down()
                        .withDetail("message", "One or more Dubbo services are unhealthy")
                        .build();
            }

            return builder.withDetail("message", "All Dubbo services are healthy")
                    .withDetail("totalServices", allMetrics.size())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Failed to check Dubbo health: " + e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
