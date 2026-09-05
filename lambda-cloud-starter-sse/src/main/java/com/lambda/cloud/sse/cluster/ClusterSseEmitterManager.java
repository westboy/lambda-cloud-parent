package com.lambda.cloud.sse.cluster;

import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.MessageType;
import com.lambda.cloud.sse.SseEmitterManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import tools.jackson.databind.ObjectMapper;

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
                        if (msg.getType() == MessageType.TO_CLIENTS) {
                            deliverToLocals(msg);
                        } else {
                            super.broadcast(msg.getEventName(), msg.getData());
                        }
                    } catch (Exception e) {
                        log.error("Cluster message dispatch failed", e);
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

    /**
     * 定向发送：本节点先投递，再经 Redis 通知其他节点——每个节点只给「clientId 在本机有连接」的用户发送，
     * 彻底解决集群部署下 sendEvent 只覆盖本节点连接的缺口。
     */
    @Override
    public void sendEvent(String clientId, String eventName, Object data) {
        super.sendEvent(clientId, eventName, data);

        if (!properties.getCluster().isEnabled() || "heartbeat".equals(eventName)) {
            return;
        }
        String json = safeToJson(data);
        ClusterMessage msg = new ClusterMessage(nodeId, MessageType.TO_CLIENTS, eventName, json, List.of(clientId));
        clusterTopic.publish(msg);
    }

    /** 将定向消息投递给本节点持有连接的目标客户端（直接调用本地发送，不再触发集群转发） */
    private void deliverToLocals(ClusterMessage msg) {
        List<String> targets = msg.getTargetClientIds();
        if (targets == null) {
            return;
        }
        for (String clientId : targets) {
            if (getActiveClients().contains(clientId)) {
                super.sendEvent(clientId, msg.getEventName(), msg.getData());
            }
        }
    }

    private String safeToJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Cluster JSON encode failed", e);
            return null;
        }
    }
}
