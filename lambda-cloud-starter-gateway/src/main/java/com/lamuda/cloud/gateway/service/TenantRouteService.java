package com.lambda.cloud.gateway.service;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;

import java.net.URI;

/**
 * TenantRouteService
 *
 * @author jpjoo
 */
public interface TenantRouteService {

    /**
     * 根据租户编号及当前请求，获取业务方提供的新URI地址
     *
     * @param tenantId tenantId
     * @param request  request
     * @return URI
     */
    URI getUri(String tenantId, ServerHttpRequest request);

    /**
     * 验证租户编号是否有效
     *
     * @param tenantId tenantId
     * @return boolean
     */
    boolean verify(@NonNull String tenantId);
}
