package com.lambda.autoconfig;

import cn.hutool.extra.spring.SpringUtil;
import com.lambda.cloud.websocket.event.StompWebSocketSubscribeEvent;
import com.lambda.cloud.websocket.handler.StompWebSocketEventHandler;
import com.lambda.cloud.websocket.interceptor.DefaultAuthenticationChannelInterceptor;
import com.lambda.cloud.websocket.interceptor.IpHandshakeInterceptor;
import com.lambda.cloud.websocket.repository.StompWebSocketChannelRepository;
import com.lambda.cloud.websocket.repository.impl.DefaultStompWebSocketChannelRepository;
import com.lambda.cloud.websocket.repository.impl.RedisStompWebSocketChannelRepository;
import com.lambda.cloud.websocket.service.StompWebSocketConnectEventService;
import com.lambda.cloud.websocket.service.impl.DefaultStompWebSocketConnectEventServiceImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "WsSessionInfo")
    private WebsocketProperties websocketProperties;

    @Autowired
    public void setWebsocketProperties(WebsocketProperties websocketProperties) {
        this.websocketProperties = websocketProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public StompWebSocketChannelRepository webSocketChannelRepository() {
        if (WebsocketProperties.ChannelStoreMode.REDIS.equals(websocketProperties.getChannelStoreMode())) {
            StringRedisTemplate template = SpringUtil.getBean(StringRedisTemplate.class);
            return new RedisStompWebSocketChannelRepository(template);
        }
        return new DefaultStompWebSocketChannelRepository(7 * 24 * 60 * 60);
    }

    @Bean
    public StompWebSocketConnectEventService wsConnectEventService(
            StompWebSocketChannelRepository wsChannelRepository) {
        return new DefaultStompWebSocketConnectEventServiceImpl(wsChannelRepository);
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
        registry.addEndpoint(websocketProperties.getStompEndpoint())
                .setAllowedOriginPatterns(websocketProperties.getOrigins())
                .addInterceptors(new IpHandshakeInterceptor())
                .withSockJS()
                .setStreamBytesLimit(524288)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30000)
                .setSessionCookieNeeded(false);
    }

    @Bean
    public StompWebSocketEventHandler wsEventHandler(
            List<StompWebSocketConnectEventService> connectEventServices,
            List<StompWebSocketSubscribeEvent> subscribeEvents) {
        return new StompWebSocketEventHandler(connectEventServices, subscribeEvents);
    }
}
