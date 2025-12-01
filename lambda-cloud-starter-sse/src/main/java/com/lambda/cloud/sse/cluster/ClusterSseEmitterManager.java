package com.lambda.cloud.sse.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.MessageType;
import com.lambda.cloud.sse.SseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClusterSseEmitterManager extends SseEmitterManager {

    private final RTopic clusterTopic;
    private final String nodeId;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClusterSseEmitterManager(SseProperties properties, RedissonClient redissonClient) {
        super(properties);
        this.nodeId = Optional.ofNullable(properties.getCluster().getNodeId())
                .orElseGet(() -> UUID.randomUUID().toString());

        String channel = properties.getCluster().getChannelPrefix() + ":broadcast";
        this.clusterTopic = redissonClient.getTopic(channel);

        // Redis 订阅
        this.clusterTopic.addListener(ClusterMessage.class, (channelName, msg) -> {
            // 不是自己发的
            if (!nodeId.equals(msg.getSourceNode())) {
                // 异步执行，避免阻塞 netty/servlet 线程
                CompletableFuture.runAsync(() -> {
                    try {
                        super.broadcast(msg.getEventName(), msg.getData());
                    } catch (Exception e) {
                        log.error("Cluster broadcast failed", e);
                    }
                });
            }
        });
    }

    @Override
    public void broadcast(String eventName, Object data) {
        super.broadcast(eventName, data);

        // 禁止集群心跳消息
        if (!properties.getCluster().isEnabled() || "heartbeat".equals(eventName)) {
            return;
        }
        String json = safeToJson(data);
        ClusterMessage msg = new ClusterMessage(nodeId, MessageType.BROADCAST, eventName, json);
        clusterTopic.publish(msg);
    }

    private String safeToJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Cluster broadcast JSON encode failed", e);
            return null;
        }
    }
}
