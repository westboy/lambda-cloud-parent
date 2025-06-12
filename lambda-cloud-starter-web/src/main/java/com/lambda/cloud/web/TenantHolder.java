package com.lambda.cloud.web;

/**
 * 当前线程租户ID持有
 *
 * <pre>
 * 系统会优先从此处获取租户ID，如果没有则从当前用户获取
 * 如果此处获取到了租户id，则authorize主库数据源会使用租户映射数据源
 * </pre>
 *
 * @author jpjoo
 */
public class TenantHolder {

	private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

	private TenantHolder() {}

	/**
	 * 设置租户ID
	 *
	 * @param tenantId the tenant id
	 */
	public static void setTenantId(String tenantId) {
		TENANT_ID.set(tenantId);
	}

	/**
	 * 获取租户ID
	 *
	 * @return the tenant id
	 */
	public static String getTenantId() {
		return TENANT_ID.get();
	}

	/**
	 * 清除租户ID
	 */
	public static void clear() {
		TENANT_ID.remove();
	}
}
