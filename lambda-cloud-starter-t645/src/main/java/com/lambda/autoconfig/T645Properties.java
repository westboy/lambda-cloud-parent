package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DL/T 645-2007 协议配置属性类。
 *
 * <p>配置前缀：{@code lambda.t645.protocol}</p>
 *
 * <p>可配置项：
 * <ul>
 *   <li>{@code basePackages} — 报文载荷扫描的包路径，默认 {@code com.lambda.cloud.t645.message}</li>
 *   <li>{@code enabled} — 是否启用协议自动扫描，默认 {@code true}</li>
 *   <li>{@code lazyInit} — 是否延迟初始化（跳过自动扫描），默认 {@code false}</li>
 *   <li>{@code failOnError} — 扫描失败时是否抛出异常中断启动，默认 {@code false}</li>
 * </ul>
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
@Data
@ConfigurationProperties(prefix = "lambda.t645.protocol")
public class T645Properties {

    /** 报文载荷扫描的包路径，支持多个包路径。 */
    private String[] basePackages = {"com.lambda.cloud.t645.message"};

    /** 是否启用 T645 协议自动扫描与注册。 */
    private boolean enabled = true;

    /** 是否延迟初始化，启用后将跳过应用启动时的自动扫描。 */
    private boolean lazyInit = false;

    /** 扫描失败时是否抛出异常以中断应用启动。 */
    private boolean failOnError = false;
}
