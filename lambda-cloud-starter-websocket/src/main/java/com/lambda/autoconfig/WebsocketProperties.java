package com.lambda.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebsocketProperties
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "lambda.websocket")
public class WebsocketProperties {

    /**
     * 用户会话存储模式
     */
    private ChannelStoreMode channelStoreMode = ChannelStoreMode.DEFAULT;
    /**
     * 是否 开启 WebSocket
     */
    private boolean enabled = true;
    /**
     * 固定前缀
     */
    private String appPrefix = "/app";
    /**
     * *用户主题前缀
     */
    private String userPrefix = "/user/";
    /**
     * *主题前缀
     */
    private String topicPrefix = "/topic/";
    /**
     * stomp websocket端点
     */
    private String stompEndpoint = "/ws/stomp";

    /**
     * 原生 websocket 端点
     */
    private String originEndpoint = "/ws/native";

    /**
     * 跨域配置
     */
    private String origins = "*";

    public enum ChannelStoreMode {
        /**
         * 基于JVM
         */
        DEFAULT,
        /**
         * 存储到Redis
         */
        REDIS
    }
}
