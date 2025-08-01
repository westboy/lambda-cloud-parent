package com.lambda.cloud.dubbo.logging;

import com.lambda.autoconfig.DubboProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo日志记录过滤器
 * <p>
 * 提供结构化的Dubbo服务调用日志记录功能，包括：
 * <ul>
 *   <li>请求开始和完成的详细日志</li>
 *   <li>服务调用耗时统计和记录</li>
 *   <li>慢调用自动检测和告警</li>
 *   <li>调用异常的详细错误日志</li>
 *   <li>远程地址和调用链路追踪</li>
 * </ul>
 * </p>
 *
 * <p>日志记录级别：</p>
 * <ul>
 *   <li><strong>INFO</strong>: 正常的服务调用开始和完成</li>
 *   <li><strong>WARN</strong>: 超过慢调用阈值的调用</li>
 *   <li><strong>ERROR</strong>: 调用失败和异常情况</li>
 * </ul>
 *
 * <p>配置控制：</p>
 * <pre>
 * lambda:
 *   dubbo:
 *     monitoring:
 *       enable-logging: true          # 是否启用日志记录
 *       slow-call-threshold: 1000     # 慢调用阈值(毫秒)
 * </pre>
 *
 * <p>日志格式示例：</p>
 * <pre>
 * INFO  - Dubbo call started - service: UserService, method: getUserById, remote: 192.168.1.100:8080
 * INFO  - Dubbo call completed - service: UserService, method: getUserById, duration: 45ms
 * WARN  - Slow Dubbo call detected - service: UserService, method: getUserById, duration: 1500ms
 * ERROR - Dubbo call failed - service: UserService, method: getUserById, duration: 200ms, error: Timeout
 * </pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboProperties.Monitoring
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "Filter requires immutable configuration object from framework")
public record LoggingFilter(DubboProperties.Monitoring monitoringProperties) implements Filter {

    /**
     * 执行日志记录过滤逻辑
     * <p>
     * 记录服务调用的完整生命周期，包括调用开始、执行时间、完成状态和异常情况。
     * 根据配置的慢调用阈值自动识别和告警慢查询。
     * </p>
     *
     * @param invoker RPC调用器，用于继续调用链
     * @param invocation RPC调用信息，包含方法名、参数等
     * @return RPC调用结果
     * @throws RpcException 当底层服务调用失败时抛出RPC异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 如果日志记录功能被禁用，直接继续调用链
        if (!monitoringProperties.isEnableLogging()) {
            return invoker.invoke(invocation);
        }

        // 记录调用开始时间
        long startTime = System.currentTimeMillis();
        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();
        String remoteAddress = extractRemoteAddress();

        // 记录调用开始日志
        log.info("Dubbo调用开始 - service: {}, method: {}, remote: {}", serviceName, methodName, remoteAddress);

        try {
            // 执行实际的服务调用
            Result result = invoker.invoke(invocation);
            long duration = System.currentTimeMillis() - startTime;

            // 根据调用耗时判断是否为慢调用
            if (duration > monitoringProperties.getSlowCallThreshold()) {
                log.warn(
                        "检测到慢调用 - service: {}, method: {}, duration: {}ms, threshold: {}ms",
                        serviceName,
                        methodName,
                        duration,
                        monitoringProperties.getSlowCallThreshold());
            } else {
                log.info("Dubbo调用完成 - service: {}, method: {}, duration: {}ms", serviceName, methodName, duration);
            }

            return result;

        } catch (RpcException e) {
            // 记录调用失败的详细信息
            long duration = System.currentTimeMillis() - startTime;
            log.error(
                    "Dubbo调用失败 - service: {}, method: {}, duration: {}ms, errorCode: {}, error: {}",
                    serviceName,
                    methodName,
                    duration,
                    e.getCode(),
                    e.getMessage());

            // 如果有嵌套异常，记录更详细的错误信息
            if (e.getCause() != null) {
                log.error(
                        "调用失败根本原因 - service: {}, method: {}, cause: {}",
                        serviceName,
                        methodName,
                        e.getCause().getClass().getSimpleName() + ": "
                                + e.getCause().getMessage());
            }

            throw e;

        } catch (Exception e) {
            // 处理非RPC异常
            long duration = System.currentTimeMillis() - startTime;
            log.error(
                    "Dubbo调用出现未知异常 - service: {}, method: {}, duration: {}ms, error: {}",
                    serviceName,
                    methodName,
                    duration,
                    e.getMessage(),
                    e);

            // 将非RPC异常包装为RPC异常
            throw new RpcException("Unexpected error during service call", e);
        }
    }

    /**
     * 提取远程地址信息
     * <p>
     * 从RPC上下文中安全地提取远程地址信息，处理地址为空的情况。
     * </p>
     *
     * @return 远程地址字符串，如果无法获取则返回"unknown"
     */
    private String extractRemoteAddress() {
        try {
            if (RpcContext.getServiceContext().getRemoteAddress() != null) {
                return RpcContext.getServiceContext().getRemoteAddress().toString();
            }
        } catch (Exception e) {
            log.debug("获取远程地址失败: {}", e.getMessage());
        }
        return "unknown";
    }
}
