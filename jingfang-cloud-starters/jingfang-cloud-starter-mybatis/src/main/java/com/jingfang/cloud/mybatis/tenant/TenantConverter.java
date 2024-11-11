package com.jingfang.cloud.mybatis.tenant;

/**
 * @author Jin
 */
@FunctionalInterface
public interface TenantConverter {
    /**
     * 将租户编号转换成租户的数据库名称
     *
     * @param id
     * @return java.lang.String
     */
    String convert(String id);
}