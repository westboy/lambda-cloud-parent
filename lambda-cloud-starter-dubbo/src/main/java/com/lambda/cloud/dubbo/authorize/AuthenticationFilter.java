package com.lambda.cloud.dubbo.authorize;

import com.lambda.autoconfig.DubboProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo认证过滤器
 * <p>
 * 负责在Dubbo服务调用链中传播和处理认证信息，包括：
 * <ul>
 *   <li>认证令牌（JWT Token）的传播</li>
 *   <li>用户身份标识的上下文传递</li>
 *   <li>租户信息的多租户隔离支持</li>
 *   <li>统一的安全上下文管理</li>
 * </ul>
 * </p>
 *
 * <p>工作原理：</p>
 * <ol>
 *   <li>从RPC上下文中提取认证信息（令牌、用户ID、租户ID）</li>
 *   <li>将认证信息设置到服务端上下文中供业务代码使用</li>
 *   <li>确保认证信息在整个调用链中的正确传播</li>
 *   <li>处理认证过程中的异常情况</li>
 * </ol>
 *
 * <p>配置控制：</p>
 * <pre>
 * lambda:
 *   dubbo:
 *     security:
 *       enabled: true                    # 是否启用认证过滤器
 *       token-header: Authorization      # 认证令牌请求头名称
 *       user-header: X-User-Id          # 用户ID请求头名称
 *       tenant-header: X-Tenant-Id      # 租户ID请求头名称
 * </pre>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 在业务代码中获取认证信息
 * String token = RpcContext.getServerContext().getAttachment("auth.token");
 * String userId = RpcContext.getServerContext().getAttachment("auth.userId");
 * String tenantId = RpcContext.getServerContext().getAttachment("auth.tenantId");
 * </pre>
 *
 * @author Lambda Cloud Team
 * @see DubboProperties.Security
 * @since 1.0.0
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "Filter requires immutable configuration object from framework")
public record AuthenticationFilter(DubboProperties.Security securityProperties) implements Filter {

    /**
     * 执行认证过滤逻辑
     * <p>
     * 从RPC请求中提取认证信息并设置到服务端上下文中，使业务代码能够
     * 访问当前请求的认证状态和用户信息。
     * </p>
     *
     * @param invoker    RPC调用器，用于继续调用链
     * @param invocation RPC调用信息，包含方法名、参数等
     * @return RPC调用结果
     * @throws RpcException 当认证处理失败时抛出RPC异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 如果安全认证功能被禁用，直接继续调用链
        if (!securityProperties.isEnabled()) {
            return invoker.invoke(invocation);
        }

        try {
            // 从RPC附件中提取认证信息
            String token = RpcContext.getServerAttachment().getAttachment(securityProperties.getTokenHeader());
            String userId = RpcContext.getServerAttachment().getAttachment(securityProperties.getUserHeader());
            String tenantId = RpcContext.getServerAttachment().getAttachment(securityProperties.getTenantHeader());

            // 将认证信息设置到服务端上下文，供业务代码访问
            if (token != null) {
                RpcContext.getServerContext().setAttachment("auth.token", token);
                log.debug("设置认证令牌到上下文 - token length: {}", token.length());
            }
            if (userId != null) {
                DubboContextHolder.setCurrentUserId(userId);
                log.debug("设置用户ID到上下文 - userId: {}", userId);
            }
            if (tenantId != null) {
                DubboContextHolder.setCurrentTenantId(tenantId);
                log.debug("设置租户ID到上下文 - tenantId: {}", tenantId);
            }

            // 记录认证上下文设置完成的调试信息
            if (log.isDebugEnabled()) {
                log.debug(
                        "认证上下文设置完成 - service: {}, method: {}, userId: {}, tenantId: {}",
                        invoker.getInterface().getSimpleName(),
                        invocation.getMethodName(),
                        userId,
                        tenantId);
            }

            // 继续执行调用链
            return invoker.invoke(invocation);

        } catch (Exception e) {
            log.error(
                    "认证过滤器处理异常 - service: {}, method: {}, error: {}",
                    invoker.getInterface().getSimpleName(),
                    invocation.getMethodName(),
                    e.getMessage(),
                    e);
            throw new RpcException("Authentication processing failed: " + e.getMessage(), e);
        }
    }
}
