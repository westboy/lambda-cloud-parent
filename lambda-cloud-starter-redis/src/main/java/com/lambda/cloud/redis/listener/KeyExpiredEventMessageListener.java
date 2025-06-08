package com.lambda.cloud.redis.listener;

import lombok.Setter;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author jpjoo
 * Redis Cluster and Pub/Sub works in two different ways. 1.User-space Pub/Sub
 * notifications are distributed across cluster nodes.That's a broadcast to all
 * nodes which makes it sufficient to subscribe on one node only to receive
 * messages 2.Key-space Pub/Sub broadcasts notifications only node-local. If a
 * key expires, the message is not broadcasted to the whole cluster. A
 * subscriber is required to subscribe to all relevant nodes to receive the
 * keyspace message.
 * <p>
 * Key-space notifications require a client to subscribe to all nodes. Cluster
 * topology changes make it hard for applications to follow changes and delete
 * invalid subscriptions/create new subscriptions.
 */
public class KeyExpiredEventMessageListener extends KeyExpirationEventMessageListener {

    public KeyExpiredEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Setter
    private RedisKeyExpiredListener publisher;

    /**
     * publish message
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        publisher.onMessage(new RedisKeyExpiredEvent<>(message.getBody()));
    }
}
