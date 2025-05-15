package com.lambda.autoconfig;

import cn.hutool.extra.spring.SpringUtil;
import com.lambda.cloud.websocket.ChannelStoreMode;
import com.lambda.cloud.websocket.event.DefaultConnectEventServiceImpl;
import com.lambda.cloud.websocket.event.WsConnectEventService;
import com.lambda.cloud.websocket.event.WsEventHandler;
import com.lambda.cloud.websocket.event.WsSubscribeEvent;
import com.lambda.cloud.websocket.interceptor.DefaultAuthenticationChannelInterceptor;
import com.lambda.cloud.websocket.interceptor.IpHandshakeInterceptor;
import com.lambda.cloud.websocket.repository.DefaultWebSocketChannelRepository;
import com.lambda.cloud.websocket.repository.RedisWebSocketChannelRepository;
import com.lambda.cloud.websocket.repository.WebSocketChannelRepository;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * WebSocketAutoConfiguration
 *
 * @author jpjoo
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(WebsocketProperties.class)
@ConditionalOnProperty(prefix = "lambda.websocket", name = "enabled", matchIfMissing = true)
public class WebSocketAutoConfiguration implements WebSocketMessageBrokerConfigurer {
    private WebsocketProperties websocketProperties;

    @Autowired
    public void setWebsocketProperties(WebsocketProperties websocketProperties) {
        this.websocketProperties = websocketProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketChannelRepository webSocketChannelRepository() {
        if (websocketProperties.channelStoreMode.equals(ChannelStoreMode.REDIS)) {
            StringRedisTemplate template = SpringUtil.getBean(StringRedisTemplate.class);
            return new RedisWebSocketChannelRepository(template);
        }
        return new DefaultWebSocketChannelRepository(7 * 24 * 60 * 60);
    }

    @Bean
    public WsConnectEventService wsConnectEventService(WebSocketChannelRepository wsChannelRepository) {
        return new DefaultConnectEventServiceImpl(wsChannelRepository);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setUserDestinationPrefix(websocketProperties.getUserPrefix());
        registry.enableSimpleBroker(websocketProperties.getTopicPrefix(), websocketProperties.getUserPrefix());
        registry.setApplicationDestinationPrefixes(websocketProperties.getAppPrefix());
    }

    @Bean
    public ChannelInterceptor channelInterceptor() {
        return new DefaultAuthenticationChannelInterceptor();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(websocketProperties.getEndpoint())
                .setAllowedOrigins(websocketProperties.getOrigins())
                .addInterceptors(new IpHandshakeInterceptor())
                .withSockJS()
                .setStreamBytesLimit(524288)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30000)
                .setSessionCookieNeeded(false);
    }

    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> webServerFactoryWebServerFactoryCustomizer() {
        return factory -> factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, 512));
            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
        });

    }

    @Bean
    public WsEventHandler wsEventHandler(List<WsConnectEventService> connectEventServices, List<WsSubscribeEvent> subscribeEvents) {
        return new WsEventHandler(connectEventServices, subscribeEvents);
    }

}
