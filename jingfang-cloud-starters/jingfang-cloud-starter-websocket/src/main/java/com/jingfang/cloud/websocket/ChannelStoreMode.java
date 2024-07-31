package com.jingfang.cloud.websocket;

/**
 * ChannelStoreMode
 *
 * @author jpjoo
 */
public enum ChannelStoreMode {
    /**
     * 基于JVM
     */
    DEFAULT,
    /**
     * 存储到Redis
     */
    REDIS
}
