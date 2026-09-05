package com.lambda.cloud.sse.cluster;

import com.lambda.cloud.sse.MessageType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Getter;

/**
 * 集群节点间通信消息
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
@Getter
public class ClusterMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String sourceNode;
    private final MessageType type;
    private final String eventName;
    private final Object data;
    private final Date timestamp;

    /** 定向消息的目标 clientId 集合（仅 MessageType.TO_CLIENTS 时使用） */
    private final List<String> targetClientIds;

    public ClusterMessage(String sourceNode, MessageType type, String eventName, Object data) {
        this(sourceNode, type, eventName, data, null);
    }

    public ClusterMessage(
            String sourceNode, MessageType type, String eventName, Object data, List<String> targetClientIds) {
        this.sourceNode = sourceNode;
        this.type = type;
        this.eventName = eventName;
        this.data = data;
        this.timestamp = new Date();
        this.targetClientIds = targetClientIds;
    }
}
