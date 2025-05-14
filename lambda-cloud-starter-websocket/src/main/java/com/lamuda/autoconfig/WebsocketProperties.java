package com.lambda.autoconfig;

import com.lambda.cloud.websocket.ChannelStoreMode;
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
    ChannelStoreMode channelStoreMode = ChannelStoreMode.DEFAULT;
    /**
     * 是否开启WebSocket
     */
    private boolean enabled = false;
    /**
     * 固定前缀
     */
    private  String appPrefix = "/app";
    /**
     * *用户主题前缀
     */
    private String userPrefix = "/user/";
    /**
     * *主题前缀
     */
    private  String topicPrefix = "/topic/";
    /**
     * *websocket端点
     */
    private String endpoint = "/ws";
    /**
     * 跨域配置
     */
    private String origins = "*";
}
