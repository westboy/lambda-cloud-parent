package com.lambda.autoconfig;

import com.lambda.cloud.dubbo.authorize.AuthenticationFilter;
import com.lambda.cloud.dubbo.logging.LoggingFilter;
import com.lambda.cloud.dubbo.authorize.TenantFilter;
import com.lambda.cloud.dubbo.health.DubboHealthIndicator;
import com.lambda.cloud.dubbo.retry.DubboRetryInterceptor;
import com.lambda.cloud.dubbo.monitor.DubboMetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Lambda Dubbo自动配置类
 * <p>
 * 提供Dubbo企业级增强功能的自动配置，包括：
 * <ul>
 *   <li>安全认证过滤器 - 处理认证令牌和用户上下文传播</li>
 *   <li>日志记录过滤器 - 提供结构化的请求/响应日志</li>
 *   <li>多租户过滤器 - 支持多租户上下文隔离和传播</li>
 *   <li>重试拦截器 - 智能重试机制</li>
 *   <li>性能监控收集器 - 收集和统计服务调用指标</li>
 * </ul>
 * </p>
 *
 * <p>
 * 该配置类在检测到Dubbo相关类存在时自动激活，并根据配置属性条件性地创建相应的Bean。
 * 所有组件都可以通过配置属性进行启用/禁用控制。
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboProperties
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "org.apache.dubbo.config.ApplicationConfig")
@EnableConfigurationProperties(DubboProperties.class)
@EnableDubbo
public class DubboAutoConfiguration {

    /**
     * 创建认证过滤器Bean
     * <p>
     * 负责处理Dubbo服务调用中的认证信息传播，包括令牌、用户ID和租户ID等。
     * 当lambda.dubbo.security.enabled=true时启用（默认启用）。
     * </p>
     *
     * @param properties Lambda Dubbo配置属性
     * @return 认证过滤器实例
     */
    @Bean
    @ConditionalOnProperty(value = "lambda.dubbo.security.enabled", havingValue = "true", matchIfMissing = true)
    public AuthenticationFilter authenticationFilter(DubboProperties properties) {
        log.info(
                "Registering Dubbo AuthenticationFilter with security config: enabled={}, tokenHeader={}",
                properties.getSecurity().isEnabled(),
                properties.getSecurity().getTokenHeader());
        return new AuthenticationFilter(properties.getSecurity());
    }

    /**
     * 创建日志记录过滤器Bean
     * <p>
     * 提供结构化的Dubbo服务调用日志记录，包括请求时间、响应时间、慢调用检测等。
     * 当lambda.dubbo.monitoring.enable-logging=true时启用（默认启用）。
     * </p>
     *
     * @param properties Lambda Dubbo配置属性
     * @return 日志记录过滤器实例
     */
    @Bean
    @ConditionalOnProperty(
            value = "lambda.dubbo.monitoring.enable-logging",
            havingValue = "true",
            matchIfMissing = true)
    public LoggingFilter loggingFilter(DubboProperties properties) {
        log.info(
                "Registering Dubbo LoggingFilter with monitoring config: slowCallThreshold={}ms",
                properties.getMonitoring().getSlowCallThreshold());
        return new LoggingFilter(properties.getMonitoring());
    }

    /**
     * 创建多租户过滤器Bean
     * <p>
     * 处理多租户上下文的隔离和传播，确保租户信息在服务调用链中正确传递。
     * 当lambda.dubbo.tenant.enabled=true时启用（默认禁用）。
     * </p>
     *
     * @param properties Lambda Dubbo配置属性
     * @return 多租户过滤器实例
     */
    @Bean
    @ConditionalOnProperty(value = "lambda.dubbo.tenant.enabled", havingValue = "true")
    public TenantFilter tenantFilter(DubboProperties properties) {
        log.info(
                "Registering Dubbo TenantFilter with tenant config: defaultTenant={}, inheritContext={}",
                properties.getTenant().getDefaultTenant(),
                properties.getTenant().isInheritTenantContext());
        return new TenantFilter(properties.getTenant());
    }

    /**
     * 创建重试拦截器Bean
     * <p>
     * 提供智能的服务调用重试机制，支持指数退避算法和可配置的重试异常类型。
     * 当lambda.dubbo.retry.enabled=true时启用（默认启用）。
     * </p>
     *
     * @param properties Lambda Dubbo配置属性
     * @return 重试拦截器实例
     */
    @Bean
    @ConditionalOnProperty(value = "lambda.dubbo.retry.enabled", havingValue = "true", matchIfMissing = true)
    public DubboRetryInterceptor dubboRetryInterceptor(DubboProperties properties) {
        log.info(
                "Registering Dubbo RetryInterceptor with retry config: maxAttempts={}, initialInterval={}ms",
                properties.getRetry().getMaxAttempts(),
                properties.getRetry().getInitialInterval());
        return new DubboRetryInterceptor(properties.getRetry());
    }

    /**
     * 创建性能监控收集器Bean
     * <p>
     * 收集和统计Dubbo服务调用的性能指标，包括请求次数、响应时间、成功率、错误统计等。
     * 当lambda.dubbo.monitoring.enable-metrics=true时启用（默认启用）。
     * 如果用户已经自定义了DubboMetricsCollector Bean，则不会创建此Bean。
     * </p>
     *
     * @param properties Lambda Dubbo配置属性
     * @return 性能监控收集器实例
     */
    @Bean
    @ConditionalOnProperty(
            value = "lambda.dubbo.monitoring.enable-metrics",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    public DubboMetricsCollector dubboMetricsCollector(DubboProperties properties) {
        log.info("Registering Dubbo MetricsCollector with monitoring enabled");
        return new DubboMetricsCollector(properties.getMonitoring());
    }

    /**
     * 创建Dubbo健康检查指示器Bean
     * <p>
     * 提供Spring Boot Actuator健康检查端点，基于服务调用指标评估Dubbo服务健康状态。
     * 当lambda.dubbo.monitoring.enabled=true时启用（默认启用）。
     * 需要同时存在指标收集器和Spring Boot Actuator。
     * </p>
     *
     * @param metricsCollector 性能监控收集器，用于获取服务指标
     * @return Dubbo健康检查指示器实例
     */
    @Bean
    @ConditionalOnProperty(
            value = "lambda.dubbo.monitoring.enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
    @ConditionalOnMissingBean
    public DubboHealthIndicator dubboHealthIndicator(DubboMetricsCollector metricsCollector) {
        log.info("Registering Dubbo HealthIndicator with metrics collector");
        return new DubboHealthIndicator(metricsCollector);
    }
}
