package com.lambda.cloud.sse.cluster;

import com.lambda.autoconfig.SseProperties;
import com.lambda.cloud.sse.MessageType;
import com.lambda.cloud.sse.SseEmitterManager;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * 分布式SSE管理器
 */
public class ClusterSseEmitterManager extends SseEmitterManager {
    private final RTopic clusterTopic;
    private final String nodeId;

    public ClusterSseEmitterManager(SseProperties properties, RedissonClient redissonClient) {
        super(properties);
        this.nodeId = java.util.UUID.randomUUID().toString();
        // 初始化集群通道
        String channelName = properties.getCluster().getChannelPrefix() + ":broadcast";
        this.clusterTopic = redissonClient.getTopic(channelName);
        // 订阅集群消息
        this.clusterTopic.addListener(ClusterMessage.class, (channel, msg) -> {
            if (msg.getType() == MessageType.BROADCAST) {
                // 忽略自己发送的消息
                if (!this.nodeId.equals(msg.getSourceNode())) {
                    super.broadcast(msg.getEventName(), msg.getData());
                }
            }
        });
    }

    @Override
    public void broadcast(String eventName, Object data) {
        super.broadcast(eventName, data);
        // 过滤心跳消息，避免集群风暴
        if (properties.getCluster().isEnabled() && !"heartbeat".equals(eventName)) {
            ClusterMessage message = new ClusterMessage(this.nodeId, MessageType.BROADCAST, eventName, data);
            clusterTopic.publish(message);
        }
    }
}
