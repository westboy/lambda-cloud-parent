package com.lambda.cloud.mybatis.purview.config;

import lombok.Getter;

/**
 * 数据权限配置持有者
 * 用于在非 Spring 管理的类中访问配置
 *
 * @author Jin
 */
public class PurviewConfigHolder {

    @Getter
    private static volatile PurviewConfig instance = new PurviewConfig();

    public static void setInstance(PurviewConfig instance) {
        if (instance != null) {
            PurviewConfigHolder.instance = instance;
        }
    }
}
