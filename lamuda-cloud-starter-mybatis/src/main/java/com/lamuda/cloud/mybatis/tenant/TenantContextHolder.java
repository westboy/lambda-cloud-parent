package com.lamuda.cloud.mybatis.tenant;


/**
 * 数据权限拦截器
 *
 * @author Jin
 */
public class TenantContextHolder implements AutoCloseable {
    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private static TenantContextHolder tenantContextHolder;

    public static TenantContextHolder getInstance() {
        if (tenantContextHolder == null) {
            return new TenantContextHolder();
        }
        return tenantContextHolder;
    }


    public void setTenantId(String tenantid) {
        TENANT.set(tenantid);
    }

    public static String getTenantId() {
        return TENANT.get();
    }

    @Override
    public void close() throws Exception {
        TENANT.remove();
    }
}
