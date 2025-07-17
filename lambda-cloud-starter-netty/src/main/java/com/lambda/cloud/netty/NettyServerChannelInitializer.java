package com.lambda.cloud.netty;

import com.lambda.cloud.netty.customizer.ChannelPipelineConfigurationCustomizer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务端ChannelInitializer
 *
 * @author Jin
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelPipelineConfigurationCustomizer channelPipelineConfigurationCustomizer;

    public NettyServerChannelInitializer(
            ChannelPipelineConfigurationCustomizer channelPipelineConfigurationCustomizer) {
        this.channelPipelineConfigurationCustomizer = channelPipelineConfigurationCustomizer;
    }

    /**
     * 初始化SocketChannel的pipeline，添加各种处理器
     *
     * @param ch 要初始化的SocketChannel
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        channelPipelineConfigurationCustomizer.configuration(pipeline);
    }
}
