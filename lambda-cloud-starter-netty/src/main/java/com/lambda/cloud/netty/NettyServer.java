package com.lambda.cloud.netty;

import com.lambda.autoconfig.NettyExtendProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

/**
 * NettyServer
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
@Configuration(proxyBeanMethods = false)
public class NettyServer implements SmartLifecycle {

    private ServerBootstrap serverBootstrap;
    private InetSocketAddress inetSocketAddress;
    private NettyExtendProperties nettyProperties;

    @Autowired
    @Qualifier("serverBootstrap")
    public void setServerBootstrap(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }

    @Autowired
    @Qualifier("nettyProperties")
    public void setNettyProperties(NettyExtendProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    @Autowired
    @Qualifier("inetSocketAddress")
    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    private Channel serverChannel;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        int port = inetSocketAddress.getPort();
        try {
            serverChannel = serverBootstrap.bind(inetSocketAddress).sync().channel();
            running.set(true);
            log.info("NettyServer started on port(s): {} (tcp)", port);
        } catch (InterruptedException e) {
            log.error("Could not initialize NettyServer", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        serverChannel.close();
        running.set(false);
        log.info("NettyServer has been stopped.");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return nettyProperties.getServer().isAutoStartUp();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}
