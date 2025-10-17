package com.lambda.cloud.websocket.event;

import com.lambda.cloud.websocket.StompWebSocketSession;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * 处理订阅相关事件
 * @author jpjoo
 */
public interface StompWebSocketSubscribeEvent {

    /**
     * 获取要处理的订阅主题
     *
     * @return String[]
     */
    String[] topics();

    /**
     * 订阅事件
     *
     * @param info:
     */
    default void subscribeEvent(StompWebSocketSession<SessionSubscribeEvent> info) {}

    /**
     * 取消订阅事件
     *
     * @param info:
     */
    default void unsubscribeEvent(StompWebSocketSession<SessionUnsubscribeEvent> info) {}
}
