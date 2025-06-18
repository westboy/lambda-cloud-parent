package com.lambda.cloud.sse.cluster;

import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.MessageType;
import com.lambda.cloud.sse.SseEmitterManager;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * 分布式SSE管理器
 */
public class ClusterSseEmitterManager extends SseEmitterManager {
    private final RTopic clusterTopic;

    public ClusterSseEmitterManager(SseProperties properties, RedissonClient redissonClient) {
        super(properties);
        // 初始化集群通道
        String channelName = properties.getCluster().getChannelPrefix() + ":broadcast";
        this.clusterTopic = redissonClient.getTopic(channelName);
        // 订阅集群消息
        this.clusterTopic.addListener(ClusterMessage.class, (channel, msg) -> {
            if (msg.getType() == MessageType.BROADCAST) {
                if (!msg.getSourceNode().equals(getNodeId())) {
                    super.broadcast(msg.getEventName(), msg.getData());
                }
            } else if (msg.getType() == MessageType.HEARTBEAT) {
                if (properties.getCluster().isSyncHeartbeat()
                        && !msg.getSourceNode().equals(getNodeId())) {
                    super.broadcast("heartbeat", "cluster-ping");
                }
            }
        });
    }

    @Override
    public void broadcast(String eventName, Object data) {
        super.broadcast(eventName, data);
        if (properties.getCluster().isEnabled()) {
            ClusterMessage message = new ClusterMessage(getNodeId(), MessageType.BROADCAST, eventName, data);
            clusterTopic.publish(message);
        }
    }

    @Override
    protected void startHeartbeatTask() {
        if (properties.getCluster().isEnabled() && properties.getCluster().isSyncHeartbeat()) {
            scheduler.scheduleAtFixedRate(
                    () -> {
                        ClusterMessage message =
                                new ClusterMessage(getNodeId(), MessageType.HEARTBEAT, "heartbeat", "cluster-ping");
                        clusterTopic.publish(message);
                    },
                    properties.getHeartbeatInterval(),
                    properties.getHeartbeatInterval(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private String getNodeId() {
        return properties.getBroadcastPath();
    }
}
