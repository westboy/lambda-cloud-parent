package com.lambda.cloud.sse.cluster;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;

/**
 * 集群节点间通信消息
 */
@Getter
public class ClusterMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        // 广播消息
        BROADCAST,
        // 心跳消息
        HEARTBEAT
    }

    // 源节点ID
    private final String sourceNode;
    // 消息类型
    private final MessageType type;
    // 事件名称
    private final String eventName;
    // 消息数据
    private final Object data;
    // 时间戳
    private final Date timestamp;

    public ClusterMessage(String sourceNode, MessageType type, String eventName, Object data) {
        this.sourceNode = sourceNode;
        this.type = type;
        this.eventName = eventName;
        this.data = data;
        this.timestamp = new Date();
    }

    @Override
    public String toString() {
        return "ClusterMessage{" + "sourceNode='"
                + sourceNode + '\'' + ", type="
                + type + ", eventName='"
                + eventName + '\'' + ", data="
                + data + ", timestamp="
                + timestamp + '}';
    }
}
