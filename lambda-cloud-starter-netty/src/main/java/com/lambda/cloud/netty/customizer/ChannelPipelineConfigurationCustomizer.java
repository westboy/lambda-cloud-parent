package com.lambda.cloud.netty.customizer;

import io.netty.channel.ChannelPipeline;

/**
 * 自定义处理ChannelPipeline
 * <p>
 * 提供自定义配置ChannelPipeline的功能
 * </p>
 * 
 * @author Jin
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
