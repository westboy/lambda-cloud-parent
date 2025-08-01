package com.lambda.cloud.dubbo.retry;

import com.lambda.autoconfig.DubboProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Dubbo智能重试拦截器
 * <p>
 * 基于Spring Retry框架为Dubbo服务调用提供智能重试机制，支持指数退避算法
 * 和可配置的重试异常类型。该拦截器只在服务消费者端生效，避免重复重试。
 * </p>
 *
 * <p>核心特性：</p>
 * <ul>
 *   <li><strong>指数退避算法</strong> - 重试间隔按指数级增长，避免雪崩效应</li>
 *   <li><strong>可配置异常类型</strong> - 只对指定的异常类型进行重试</li>
 *   <li><strong>最大重试限制</strong> - 防止无限重试，可配置最大重试次数</li>
 *   <li><strong>重试间隔控制</strong> - 支持初始间隔、倍数和最大间隔配置</li>
 *   <li><strong>智能重试日志</strong> - 详细记录每次重试的执行情况</li>
 * </ul>
 *
 * <p>重试策略配置：</p>
 * <pre>
 * lambda:
 *   dubbo:
 *     retry:
 *       enabled: true                    # 是否启用重试机制
 *       max-attempts: 3                  # 最大重试次数(包含首次调用)
 *       initial-interval: 1000           # 初始重试间隔(毫秒)
 *       multiplier: 2.0                  # 重试间隔倍数
 *       max-interval: 10000              # 最大重试间隔(毫秒)
 *       retryable-exceptions:            # 可重试的异常类型
 *         - java.util.concurrent.TimeoutException
 *         - java.net.SocketTimeoutException
 * </pre>
 *
 * <p>重试算法示例：</p>
 * <pre>
 * 第1次调用失败 -> 等待1000ms -> 第2次重试
 * 第2次调用失败 -> 等待2000ms -> 第3次重试
 * 第3次调用失败 -> 不再重试，抛出异常
 * </pre>
 *
 * <p>适用场景：</p>
 * <ul>
 *   <li>网络抖动导致的临时性连接失败</li>
 *   <li>服务端临时不可用或过载</li>
 *   <li>超时异常但服务端可能正在处理</li>
 *   <li>其他可恢复的瞬态故障</li>
 * </ul>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>只在消费者端生效，避免服务端也进行重试造成放大效应</li>
 *   <li>只对配置的异常类型进行重试，避免对业务异常的误重试</li>
 *   <li>重试会增加总的调用时间，需要合理配置重试参数</li>
 *   <li>对于幂等性要求较高的操作，使用重试机制需要谨慎</li>
 * </ul>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboProperties.Retry
 * @see RetryTemplate
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER})
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Retry interceptor requires configuration object from framework")
public class DubboRetryInterceptor implements Filter {

    private final DubboProperties.Retry retryProperties;
    private final RetryTemplate retryTemplate;

    public DubboRetryInterceptor(DubboProperties.Retry retryProperties) {
        this.retryProperties = retryProperties;
        this.retryTemplate = createRetryTemplate();
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        for (String exceptionName : retryProperties.getRetryableExceptions()) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> exceptionClass = (Class<? extends Throwable>) Class.forName(exceptionName);
                retryableExceptions.put(exceptionClass, true);
            } catch (ClassNotFoundException e) {
                log.warn("Retryable exception class not found: {}", exceptionName);
            }
        }

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(retryProperties.getMaxAttempts(), retryableExceptions);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getInitialInterval());
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());
        backOffPolicy.setMaxInterval(retryProperties.getMaxInterval());

        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (!retryProperties.isEnabled()) {
            return invoker.invoke(invocation);
        }

        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();

        try {
            return retryTemplate.execute((RetryCallback<Result, RpcException>) context -> {
                if (context.getRetryCount() > 0) {
                    log.info(
                            "Retrying Dubbo call - service: {}, method: {}, attempt: {}",
                            serviceName,
                            methodName,
                            context.getRetryCount() + 1);
                }
                return invoker.invoke(invocation);
            });
        } catch (Exception e) {
            log.error(
                    "Dubbo call failed after {} retries - service: {}, method: {}",
                    retryProperties.getMaxAttempts(),
                    serviceName,
                    methodName,
                    e);
            if (e instanceof RpcException) {
                throw (RpcException) e;
            }
            throw new RpcException("Retry failed: " + e.getMessage(), e);
        }
    }
}
