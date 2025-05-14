package com.lambda.cloud.redis.delay;

/**
 * @author westboy
 */
public interface RedisDelayedListener<T> {

    /**
     * 处理延迟任务
     *
     * @param obj
     */
    void execute(T obj);
}
