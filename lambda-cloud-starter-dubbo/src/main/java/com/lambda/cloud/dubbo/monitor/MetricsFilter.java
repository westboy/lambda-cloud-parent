package com.lambda.cloud.dubbo.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo性能指标过滤器
 * <p>
 * 作为Dubbo调用链中的性能指标收集入口，负责拦截所有的RPC调用并收集性能数据。
 * 该过滤器会自动记录每次调用的耗时、成功失败状态、异常信息等关键指标。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li><strong>调用时间统计</strong> - 精确记录每次RPC调用的开始和结束时间</li>
 *   <li><strong>成功失败统计</strong> - 统计调用的成功率和失败率</li>
 *   <li><strong>异常信息收集</strong> - 捕获并记录调用过程中的异常类型和详情</li>
 *   <li><strong>无侵入性监控</strong> - 透明地收集指标，不影响原有业务逻辑</li>
 * </ul>
 *
 * <p>过滤器执行流程：</p>
 * <ol>
 *   <li>检查指标收集器是否可用，如果不可用则直接透传</li>
 *   <li>记录调用开始时间</li>
 *   <li>执行实际的服务调用</li>
 *   <li>捕获调用结果（成功/失败）和异常信息</li>
 *   <li>计算调用耗时并记录到指标收集器</li>
 * </ol>
 *
 * <p>与指标收集器的集成：</p>
 * <p>
 * 该过滤器依赖于{@link DubboMetricsCollector}来存储和管理性能指标数据。
 * 如果指标收集器未配置或不可用，过滤器会优雅地降级，不影响正常的服务调用。
 * </p>
 *
 * <p>激活条件：</p>
 * <p>
 * 通过{@code @Activate}注解自动激活，同时在服务提供者和消费者端生效，
 * 确保完整的调用链路都能被监控。
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboMetricsCollector
 * @see DubboMetricsCollector.ServiceMetrics
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public record MetricsFilter(DubboMetricsCollector metricsCollector) implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (metricsCollector == null) {
            return invoker.invoke(invocation);
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;
        Throwable error = null;

        try {
            Result result = invoker.invoke(invocation);
            success = true;
            return result;
        } catch (RpcException e) {
            error = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordRequest(invoker, invocation, duration, success, error);
        }
    }
}
