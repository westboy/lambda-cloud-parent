package com.lambda.cloud.redis.listener;

import org.springframework.data.redis.core.RedisKeyExpiredEvent;

/**
 * @author westboy
 */
public interface RedisKeyExpiredListener {
    /**
     * 接收RedisKey失效事件
     * @param event e
     */
    void onMessage(RedisKeyExpiredEvent<String> event);
}
