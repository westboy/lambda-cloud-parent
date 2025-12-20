package com.lambda.cloud.mybatis.tenant;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 租户上下文管理器（线程安全增强版）
 *
 * @author Jin
 */
public final class TenantContextHolder implements AutoCloseable {

    private static final ThreadLocal<String> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 静态内部类实现单例
     */
    private static class Holder {
        static final TenantContextHolder INSTANCE = new TenantContextHolder();
    }

    /**
     * 获取单例实例（推荐使用 try-with-resources）
     */
    public static TenantContextHolder getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 设置当前租户ID（非空校验）
     */
    public void setTenantId(String tenantId) {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID（带默认值）
     */
    public static String getCurrentTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 安全执行带租户上下文的代码块
     */
    public static <T> T runWithTenant(String tenantId, Callable<T> task) throws Exception {
        try (TenantContextHolder holder = getInstance()) {
            holder.setTenantId(tenantId);
            return task.call();
        }
    }

    @Override
    public void close() {
        TENANT_ID_HOLDER.remove();
        if (TENANT_ID_HOLDER.get() != null) {
            TENANT_ID_HOLDER.remove();
        }
    }
}
