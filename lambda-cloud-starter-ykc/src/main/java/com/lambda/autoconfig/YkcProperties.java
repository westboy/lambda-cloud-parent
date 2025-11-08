package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 协议扫描器配置属性
 * <p>
 * 配置协议扫描器的行为，包括扫描路径、启用状态等
 * </p>
 *
 * @author Lambda
 */
@Data
@ConfigurationProperties(prefix = "lambda.protocol.scanner")
public class YkcProperties {

    /**
     * 是否启用协议自动扫描
     */
    private boolean enabled = true;

    /**
     * 要扫描的基础包路径
     * <p>
     * 示例: com.lambda.cloud, com.example.protocol
     * </p>
     */
    private String[] basePackages;

    /**
     * 是否延迟初始化
     * <p>
     * 如果为 true，则不会在应用启动时自动扫描，需要手动调用扫描方法
     * </p>
     */
    private boolean lazyInit = false;

    /**
     * 扫描失败时是否抛出异常
     * <p>
     * 如果为 false，扫描失败时只记录日志，不会中断应用启动
     * </p>
     */
    private boolean failOnError = false;

    /**
     * 是否扫描子包
     */
    private boolean scanSubPackages = true;

    /**
     * 排除的包路径
     * <p>
     * 在扫描时会跳过这些包
     * </p>
     */
    private String[] excludePackages;

    /**
     * 是否打印详细日志
     */
    private boolean verbose = false;
}
