package com.lamuda.cloud.websocket.event;


import com.lamuda.cloud.websocket.WsSessionInfo;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 处理ws连接对应事件
 *
 * @author jpjoo
 */
public interface WsConnectEventService {

    /**
     * 正在建立连接
     *
     * @param info:
     */
    default void connectEvent(WsSessionInfo<SessionConnectEvent> info) {}

    /**
     * 建立连接完成
     *
     * @param info:
     */
    default void connectedEvent(WsSessionInfo<SessionConnectedEvent> info) {}

    /**
     * 关闭连接（包括浏览器关闭当前tab页、刷新等）
     *
     * @param info:
     */
    default void disconnectEvent(WsSessionInfo<SessionDisconnectEvent> info) {}
}
