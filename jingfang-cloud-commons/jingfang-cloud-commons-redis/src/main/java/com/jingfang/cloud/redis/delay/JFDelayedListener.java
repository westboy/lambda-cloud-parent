package com.jingfang.cloud.redis.delay;

/**
 * @author westboy
 */
public interface JFDelayedListener<T> {

    /**
     * 处理延迟任务
     *
     * @param obj
     */
    void execute(T obj);
}
