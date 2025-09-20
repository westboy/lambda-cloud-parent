package com.lambda.cloud.core.shared;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import java.util.List;
import lombok.Data;

/**
 * CORS（跨域资源共享）配置属性类
 * <p>
 * 该类用于配置Web应用的跨域访问策略，包括允许的源、方法、头部等信息。
 * CORS是一种机制，允许Web应用从不同域名访问资源。
 *
 * <p>主要配置项：
 * <ul>
 *     <li>是否启用CORS</li>
 *     <li>允许的源域名列表</li>
 *     <li>预检请求的缓存时间</li>
 *     <li>允许的HTTP方法（预定义）</li>
 *     <li>允许的请求头（预定义）</li>
 *     <li>暴露的响应头（预定义）</li>
 * </ul>
 *
 * <p>使用不可变集合来确保配置的安全性和线程安全性。
 *
 * @author lambda
 * @since 1.0.0
 */
@Data
public class CorsProperty {

    /**
     * 匹配所有路径的通配符
     */
    public static final String ALL_PATH = "/**";

    /**
     * 匹配所有值的通配符
     */
    public static final String ALL = "*";

    /**
     * 允许的HTTP方法集合
     * <p>包含常用的RESTful API方法：DELETE、GET、HEAD、OPTIONS、POST、PUT、TRACE、PATCH
     */
    public static final ImmutableSet<String> ALLOWED_METHOD = ImmutableSet.copyOf(new String[] {
        "DELETE", "GET", "HEAD", "OPTIONS", "POST", "PUT", "TRACE", "PATCH",
    });

    /**
     * 暴露给客户端的响应头集合
     * <p>客户端JavaScript可以访问这些响应头
     */
    public static final ImmutableSet<String> EXPOSED_HEADERS =
            ImmutableSet.copyOf(new String[] {HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN});

    /**
     * 允许的请求头集合
     * <p>包含常用的请求头：Authorization、Content-Type、X-Content-Type、x-requested-with
     */
    public static final ImmutableSet<String> ALLOWED_HEADERS = ImmutableSet.copyOf(
            new String[] {HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Content-Type", "x-requested-with"});

    /**
     * 是否启用CORS支持
     * <p>默认为false，需要显式启用
     */
    private boolean enabled;

    /**
     * 允许的源域名列表
     * <p>指定哪些域名可以访问资源，使用不可变列表确保安全性
     */
    private List<String> allowedOrigins;

    /**
     * 预检请求的缓存时间（秒）
     * <p>浏览器会缓存预检请求的结果，避免频繁发送OPTIONS请求
     * 默认值为3600秒（1小时）
     */
    private long maxAge = 3600L;

    /**
     * 设置允许的源域名列表
     * <p>
     * 使用不可变列表来确保配置的安全性，防止外部修改。
     * 如果传入null，则设置为空的不可变列表。
     *
     * @param allowedOrigins 允许的源域名列表，可以为null
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins == null) {
            this.allowedOrigins = ImmutableList.of();
        } else {
            this.allowedOrigins = ImmutableList.copyOf(allowedOrigins);
        }
    }
}
