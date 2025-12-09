package com.lambda.cloud.cache.support;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存键
     */
    private Object key;

    /**
     * 源节点ID(用于避免回环清除)
     */
    private String sourceNodeId;

    /**
     * 批量操作的键集合
     */
    private java.util.Set<Object> keys;

    private Type type;

    public enum Type {
        PUT,
        PUT_ALL,
        EVICT,
        EVICT_ALL,
        CLEAR
    }
}
