package com.lambda.cloud.dubbo.authorize;

import com.lambda.autoconfig.DubboProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo多租户过滤器
 * <p>
 * 负责在Dubbo服务调用链中处理多租户上下文的隔离和传播，确保租户信息
 * 在分布式服务调用中正确传递，并实现多租户数据隔离。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li><strong>租户上下文传播</strong> - 自动传播当前租户信息到下游服务</li>
 *   <li><strong>租户信息接收</strong> - 从上游请求中提取并设置租户上下文</li>
 *   <li><strong>默认租户处理</strong> - 当请求中无租户信息时使用默认租户</li>
 *   <li><strong>上下文清理</strong> - 请求完成后自动清理租户上下文</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>检查当前线程是否已有租户上下文，如果有且启用继承，则传播到下游</li>
 *   <li>从RPC请求中提取租户ID，并设置到当前线程上下文</li>
 *   <li>如果既无当前上下文也无请求租户ID，则使用默认租户</li>
 *   <li>执行实际的服务调用</li>
 *   <li>请求完成后清理租户上下文，避免线程污染</li>
 * </ol>
 *
 * <p>配置示例：</p>
 * <pre>
 * lambda:
 *   dubbo:
 *     tenant:
 *       enabled: true                    # 启用多租户支持
 *       tenant-id-header: X-Tenant-Id   # 租户ID请求头名称
 *       default-tenant: default          # 默认租户ID
 *       inherit-tenant-context: true     # 是否继承租户上下文
 * </pre>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 在业务代码中获取当前租户
 * String currentTenant = DubboContextUtils.getCurrentTenantId();
 *
 * // 手动设置租户（通常在认证后）
 * DubboContextUtils.setCurrentTenantId("tenant-001");
 * </pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see DubboProperties.Tenant
 * @see DubboContextHolder
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public record TenantFilter(DubboProperties.Tenant tenantProperties) implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (!tenantProperties.isEnabled()) {
            return invoker.invoke(invocation);
        }

        try {
            String currentTenantId = DubboContextHolder.getCurrentTenantId();

            if (tenantProperties.isInheritTenantContext() && currentTenantId != null) {
                RpcContext.getClientAttachment().setAttachment(tenantProperties.getTenantIdHeader(), currentTenantId);
                log.debug("Tenant context propagated: {}", currentTenantId);
            }

            String incomingTenantId =
                    RpcContext.getServerAttachment().getAttachment(tenantProperties.getTenantIdHeader());

            if (incomingTenantId != null) {
                DubboContextHolder.setCurrentTenantId(incomingTenantId);
                log.debug("Tenant context received: {}", incomingTenantId);
            } else if (currentTenantId == null) {
                DubboContextHolder.setCurrentTenantId(tenantProperties.getDefaultTenant());
                log.debug("Using default tenant: {}", tenantProperties.getDefaultTenant());
            }

            return invoker.invoke(invocation);
        } catch (Exception e) {
            log.error("Tenant filter error", e);
            throw new RpcException("Tenant context processing failed: " + e.getMessage());
        } finally {
            if (RpcContext.getServerAttachment().getAttachment(tenantProperties.getTenantIdHeader()) != null) {
                DubboContextHolder.clearCurrentTenantId();
            }
        }
    }
}
