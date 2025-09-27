package com.lambda.cloud.netty.customizer;

import io.netty.channel.ChannelPipeline;

/**
 * 自定义处理ChannelPipeline
 */
@FunctionalInterface
public interface ChannelPipelineConfigurationCustomizer {
    /**
     * 自定义配置
     *
     * @param pipeline：
     */
    void configuration(ChannelPipeline pipeline);
}
