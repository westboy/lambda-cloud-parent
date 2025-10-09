package com.lambda.autoconfig;

import com.lambda.cloud.netty.NettyServer;
import com.lambda.cloud.netty.NettyServerChannelInitializer;
import com.lambda.cloud.netty.customizer.ChannelPipelineConfigurationCustomizer;
import com.lambda.cloud.netty.customizer.ServerBootstrapConfigurationCustomizer;
import com.lambda.cloud.netty.repository.ChannelRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Slf4j
@AutoConfiguration
@Import({NettyServer.class})
@EnableConfigurationProperties(NettyExtendProperties.class)
public class NettyAutoConfiguration {

    @Bean
    @Primary
    public NettyExtendProperties nettyProperties() {
        return new NettyExtendProperties();
    }

    @Bean("shouldEpoll")
    public Boolean shouldEpoll() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("linux") && Epoll.isAvailable();
    }

    @Bean("serverBootstrap")
    @ConditionalOnMissingBean
    public ServerBootstrap bootstrap(
            NettyExtendProperties nettyProperties,
            NettyServerChannelInitializer channelInitializer,
            ServerBootstrapConfigurationCustomizer serverBootstrapConfigurationCustomizer,
            @Qualifier("bossGroup") EventLoopGroup bossGroup,
            @Qualifier("workerGroup") EventLoopGroup workerGroup,
            Boolean shouldEpoll) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(shouldEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(channelInitializer);
        nettyProperties
                .getServer()
                .getOptionMap()
                .forEach((k, v) -> serverBootstrap.option(ChannelOption.valueOf(k), v));
        serverBootstrapConfigurationCustomizer.configuration(serverBootstrap);
        return serverBootstrap;
    }

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup(Boolean shouldEpoll) {
        return shouldEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup(NettyExtendProperties nettyProperties, Boolean shouldEpoll) {
        if (nettyProperties.getServer().getWorkerThreadCount() > 0) {
            return shouldEpoll
                    ? new EpollEventLoopGroup(nettyProperties.getServer().getWorkerThreadCount())
                    : new NioEventLoopGroup(nettyProperties.getServer().getWorkerThreadCount());
        }
        return shouldEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @Bean("inetSocketAddress")
    @ConditionalOnMissingBean
    public InetSocketAddress tcpPort(NettyExtendProperties nettyProperties) {
        return new InetSocketAddress(nettyProperties.getServer().getTcpPort());
    }

    @Bean("channelInitializer")
    @ConditionalOnMissingBean
    public NettyServerChannelInitializer nettyServerChannelInitializer(
            ChannelPipelineConfigurationCustomizer channelPipelineConfigurationCustomizer) {
        return new NettyServerChannelInitializer(channelPipelineConfigurationCustomizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerBootstrapConfigurationCustomizer serverBootstrapConfigurationCustomizer() {
        return serverBootstrap -> log.info("NettyServerConfigurationCustomizer");
    }

    @Bean
    public ChannelRepository channelRepository() {
        return new ChannelRepository();
    }
}
