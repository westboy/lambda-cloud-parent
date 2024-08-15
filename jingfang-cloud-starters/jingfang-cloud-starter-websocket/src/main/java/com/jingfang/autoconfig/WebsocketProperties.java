package com.jingfang.autoconfig;

import com.jingfang.cloud.websocket.ChannelStoreMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebsocketProperties
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "jingfang.websocket")
public class WebsocketProperties {

    /**
     * 是否开启WebSocket
     */
    boolean enabled = false;
    /**
     * 固定前缀
     */
    String appPrefix = "/app";
    /**
     * *用户主题前缀
     */
    String userPrefix = "/user/";
    /**
     * *主题前缀
     */
    String topicPrefix = "/topic/";
    /**
     * *websocket端点
     */
    String endpoint = "/ws";
    /**
     * 用户会话存储模式
     */
    ChannelStoreMode channelStoreMode = ChannelStoreMode.DEFAULT;
}