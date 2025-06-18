package com.lambda.cloud.sse;

/**
 * SseEventListener
 *
 * @author Jin
 */
public interface SseEventListener {
    /**
     * 当新连接建立时触发
     *
     * @param clientId 客户端ID
     */
    default void onConnect(String clientId) {}

    /**
     * 当连接断开时触发
     *
     * @param clientId 客户端ID
     */
    default void onDisconnect(String clientId) {}

    /**
     * 当发送消息时触发
     *
     * @param clientId  客户端ID
     * @param eventName 事件名称
     */
    default void onMessageSent(String clientId, String eventName) {}
}
