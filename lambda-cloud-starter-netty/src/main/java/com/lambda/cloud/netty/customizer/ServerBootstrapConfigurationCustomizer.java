package com.lambda.cloud.netty.customizer;

import io.netty.bootstrap.ServerBootstrap;

/**
 * 自定义处理ServerBootstrap
 *
 * @author Jin
 */
@FunctionalInterface
public interface ServerBootstrapConfigurationCustomizer {

    /**
     * 自定义配置
     *
     * @param serverBootstrap：
     */
    void configuration(ServerBootstrap serverBootstrap);
}
