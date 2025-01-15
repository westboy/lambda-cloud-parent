package com.jingfang.cloud.gateway.service;

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

/**
 * 增加路由
 *
 * @author jpjoo
 */
public interface RouterEnhancer {

    /**
     * 配置新增加的路由
     *
     */
    void config(RouteLocatorBuilder.Builder builder);
}
