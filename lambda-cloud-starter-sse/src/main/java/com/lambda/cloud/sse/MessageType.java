package com.lambda.cloud.sse;

/**
 * MessageType
 * author Jin
 */
public enum MessageType {
    BROADCAST,
    HEARTBEAT,

    /** 定向消息：投递给 targetClientIds 中在本节点有连接的客户端 */
    TO_CLIENTS
}
