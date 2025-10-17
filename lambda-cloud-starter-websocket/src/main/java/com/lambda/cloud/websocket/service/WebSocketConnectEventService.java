package com.lambda.cloud.websocket.service;

import com.lambda.cloud.websocket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 处理ws连接对应事件
 *
 * @author jpjoo
 */
public interface WebSocketConnectEventService {

    /**
     * 正在建立连接
     *
     * @param info:
     */
    default void connectEvent(WebSocketSession<SessionConnectEvent> info) {}

    /**
     * 建立连接完成
     *
     * @param info:
     */
    default void connectedEvent(WebSocketSession<SessionConnectedEvent> info) {}

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param info:
     */
    default void disconnectEvent(WebSocketSession<SessionDisconnectEvent> info) {}
}
