package com.lambda.cloud.dubbo.authorize;

import org.apache.dubbo.rpc.RpcContext;

/**
 * Dubbo上下文工具类
 * <p>
 * 提供便捷的Dubbo RPC上下文操作方法，用于在服务调用链中传递和管理上下文信息。
 * 主要功能包括租户信息、用户信息、链路追踪ID的设置、获取和清理操作。
 * </p>
 *
 * <p>上下文传播机制：</p>
 * <ul>
 *   <li><strong>服务端上下文</strong>: 用于当前服务接收和读取上下文信息</li>
 *   <li><strong>客户端上下文</strong>: 用于向下游服务传播上下文信息</li>
 *   <li><strong>自动传播</strong>: 设置方法会同时更新服务端和客户端上下文</li>
 * </ul>
 *
 * <p>典型使用场景：</p>
 * <ul>
 *   <li>多租户系统中的租户信息传播</li>
 *   <li>用户身份信息在微服务间的传递</li>
 *   <li>分布式链路追踪ID的管理</li>
 *   <li>调用链路的网络地址获取</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 设置上下文信息
 * DubboContextUtils.setCurrentTenantId("tenant-001");
 * DubboContextUtils.setCurrentUserId("user-123");
 * DubboContextUtils.setCurrentTraceId("trace-456");
 *
 * // 在业务代码中获取上下文信息
 * String tenantId = DubboContextUtils.getCurrentTenantId();
 * String userId = DubboContextUtils.getCurrentUserId();
 *
 * // 请求完成后清理上下文
 * DubboContextUtils.clearContext();
 * </pre>
 *
 * <p>线程安全性：</p>
 * <p>
 * 该工具类基于Dubbo的RpcContext实现，RpcContext是线程本地的，
 * 因此在同一线程内的操作是安全的，不会影响其他线程的上下文。
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see RpcContext
 */
public class DubboContextHolder {

    /** 租户ID在RPC上下文中的键名 */
    private static final String TENANT_ID_KEY = "tenantId";

    /** 用户ID在RPC上下文中的键名 */
    private static final String USER_ID_KEY = "userId";

    /** 链路追踪ID在RPC上下文中的键名 */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 获取当前请求的租户ID
     * <p>
     * 从服务端RPC上下文中获取租户标识，用于多租户系统中的数据隔离。
     * </p>
     *
     * @return 当前租户ID，如果未设置则返回null
     */
    public static String getCurrentTenantId() {
        return RpcContext.getServiceContext().getAttachment(TENANT_ID_KEY);
    }

    /**
     * 设置当前请求的租户ID
     * <p>
     * 同时设置服务端和客户端上下文，确保租户信息能够传播到下游服务。
     * 通常在请求入口处设置，用于整个调用链的租户隔离。
     * </p>
     *
     * @param tenantId 租户标识，不能为null
     * @throws IllegalArgumentException 如果tenantId为null或空字符串
     */
    public static void setCurrentTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }
        RpcContext.getServiceContext().setAttachment(TENANT_ID_KEY, tenantId);
        RpcContext.getClientAttachment().setAttachment(TENANT_ID_KEY, tenantId);
    }

    /**
     * 获取当前请求的用户ID
     * <p>
     * 从服务端RPC上下文中获取用户标识，用于用户身份识别和权限控制。
     * </p>
     *
     * @return 当前用户ID，如果未设置则返回null
     */
    public static String getCurrentUserId() {
        return RpcContext.getServiceContext().getAttachment(USER_ID_KEY);
    }

    /**
     * 设置当前请求的用户ID
     * <p>
     * 同时设置服务端和客户端上下文，确保用户身份信息能够传播到下游服务。
     * 通常在用户认证后设置，用于整个调用链的用户身份识别。
     * </p>
     *
     * @param userId 用户标识，不能为null
     * @throws IllegalArgumentException 如果userId为null或空字符串
     */
    public static void setCurrentUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        RpcContext.getServiceContext().setAttachment(USER_ID_KEY, userId);
        RpcContext.getClientAttachment().setAttachment(USER_ID_KEY, userId);
    }

    /**
     * 获取当前请求的链路追踪ID
     * <p>
     * 从服务端RPC上下文中获取链路追踪标识，用于分布式系统的调用链追踪。
     * </p>
     *
     * @return 当前链路追踪ID，如果未设置则返回null
     */
    public static String getCurrentTraceId() {
        return RpcContext.getServiceContext().getAttachment(TRACE_ID_KEY);
    }

    /**
     * 设置当前请求的链路追踪ID
     * <p>
     * 同时设置服务端和客户端上下文，确保追踪ID能够传播到下游服务。
     * 通常在请求入口处生成并设置，用于整个调用链的链路追踪。
     * </p>
     *
     * @param traceId 链路追踪标识，不能为null
     * @throws IllegalArgumentException 如果traceId为null或空字符串
     */
    public static void setCurrentTraceId(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            throw new IllegalArgumentException("TraceId cannot be null or empty");
        }
        RpcContext.getServiceContext().setAttachment(TRACE_ID_KEY, traceId);
        RpcContext.getClientAttachment().setAttachment(TRACE_ID_KEY, traceId);
    }

    /**
     * 清理当前租户ID
     * <p>
     * 从服务端RPC上下文中移除租户ID信息。
     * 通常在请求处理完成后调用，避免租户信息泄露到下一个请求。
     * </p>
     */
    public static void clearCurrentTenantId() {
        RpcContext.getServiceContext().removeAttachment(TENANT_ID_KEY);
    }

    /**
     * 清理当前用户ID
     * <p>
     * 从服务端RPC上下文中移除用户ID信息。
     * 通常在请求处理完成后调用，避免用户信息泄露到下一个请求。
     * </p>
     */
    public static void clearCurrentUserId() {
        RpcContext.getServiceContext().removeAttachment(USER_ID_KEY);
    }

    /**
     * 清理当前线程的上下文信息
     * <p>
     * 从服务端RPC上下文中移除租户ID、用户ID和链路追踪ID等信息。
     * 通常在请求处理完成后调用，避免上下文信息泄露到下一个请求。
     * </p>
     *
     * <p><strong>重要提示</strong>：在Web应用中，建议在请求结束时调用此方法，
     * 特别是在使用线程池的环境中，确保线程复用时不会携带上一个请求的上下文信息。</p>
     */
    public static void clearContext() {
        RpcContext.getServiceContext().removeAttachment(TENANT_ID_KEY);
        RpcContext.getServiceContext().removeAttachment(USER_ID_KEY);
        RpcContext.getServiceContext().removeAttachment(TRACE_ID_KEY);
    }

    /**
     * 获取远程调用者的网络地址
     * <p>
     * 返回当前RPC调用的远程客户端地址信息，用于日志记录、监控和安全审计。
     * </p>
     *
     * @return 远程地址字符串(格式: host:port)，如果无法获取则返回"unknown"
     */
    public static String getRemoteAddress() {
        try {
            return RpcContext.getServiceContext().getRemoteAddress() != null
                    ? RpcContext.getServiceContext().getRemoteAddress().toString()
                    : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取本地服务的网络地址
     * <p>
     * 返回当前服务实例的本地地址信息，用于服务发现、负载均衡和监控。
     * </p>
     *
     * @return 本地地址字符串(格式: host:port)，如果无法获取则返回"unknown"
     */
    public static String getLocalAddress() {
        try {
            return RpcContext.getServiceContext().getLocalAddress() != null
                    ? RpcContext.getServiceContext().getLocalAddress().toString()
                    : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 检查是否存在有效的上下文信息
     * <p>
     * 判断当前是否至少设置了租户ID、用户ID或链路追踪ID中的一个。
     * </p>
     *
     * @return 如果存在任何上下文信息则返回true，否则返回false
     */
    public static boolean hasContext() {
        return getCurrentTenantId() != null || getCurrentUserId() != null || getCurrentTraceId() != null;
    }

    /**
     * 获取当前上下文的简要描述
     * <p>
     * 返回包含租户ID、用户ID和链路追踪ID的格式化字符串，用于日志输出和调试。
     * </p>
     *
     * @return 上下文描述字符串，格式: "tenant=xxx, user=xxx, trace=xxx"
     */
    public static String getContextInfo() {
        return String.format(
                "tenant=%s, user=%s, trace=%s", getCurrentTenantId(), getCurrentUserId(), getCurrentTraceId());
    }
}
