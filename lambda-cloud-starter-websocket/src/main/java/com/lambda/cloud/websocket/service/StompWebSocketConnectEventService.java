package com.lambda.cloud.websocket.service;

import com.lambda.cloud.websocket.session.StompWebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 处理ws连接对应事件
 *
 * @author jpjoo
 */
public interface StompWebSocketConnectEventService {

    /**
     * 正在建立连接
     *
     * @param info:
     */
    default void connectEvent(StompWebSocketSession<SessionConnectEvent> info) {}

    /**
     * 建立连接完成
     *
     * @param info:
     */
    default void connectedEvent(StompWebSocketSession<SessionConnectedEvent> info) {}

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param info:
     */
    default void disconnectEvent(StompWebSocketSession<SessionDisconnectEvent> info) {}
}
