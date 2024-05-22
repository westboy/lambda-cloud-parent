package com.jingfang.cloud.redis.delay;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author westboy
 */
@Data
public class RedisDelayConfig {

    /**
     * 延迟队列名称
     */
    String name;
    /**
     * 延迟时间，单位(秒)
     */
    int delay;
    /**
     * 单位，默认为毫秒
     */
    TimeUnit timeUnit = TimeUnit.SECONDS;
    /**
     * 工作线程数
     */
    int works = 1;
    /**
     * 当队列中的值超过该值时将产生告警
     */
    int threshold = 1000;
}
