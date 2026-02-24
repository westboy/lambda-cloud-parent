package com.lambda.cloud.oss.manager;

import com.lambda.cloud.oss.client.OssClient;
import com.lambda.cloud.oss.exception.OssException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * OSS 客户端管理器
 * 负责管理多个 OSS 客户端实例
 * 
 * @author jpjoo
 */
@Slf4j
public class OssClientManager {

    private static final Map<String, OssClient> CLIENT_CACHE = new ConcurrentHashMap<>();
    
    private static final String DEFAULT_CLIENT_NAME = "default";

    /**
     * 注册客户端
     * 
     * @param clientName 客户端名称
     * @param ossClient 客户端实例
     * @throws IllegalArgumentException 如果参数无效
     */
    public void register(String clientName, OssClient ossClient) {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new IllegalArgumentException("客户端名称不能为空");
        }
        if (ossClient == null) {
            throw new IllegalArgumentException("客户端实例不能为 null");
        }
        
        OssClient existing = CLIENT_CACHE.put(clientName, ossClient);
        if (existing != null) {
            log.warn("客户端 [{}] 已存在，已被覆盖", clientName);
        } else {
            log.info("注册 OSS 客户端: {}", clientName);
        }
    }

    /**
     * 获取客户端
     * 
     * @param clientName 客户端名称
     * @return 客户端实例
     * @throws OssException 客户端不存在
     */
    public OssClient get(String clientName) {
        OssClient client = CLIENT_CACHE.get(clientName);
        if (client == null) {
            throw new OssException("OSS 客户端不存在: " + clientName + 
                ", 可用的客户端: " + CLIENT_CACHE.keySet());
        }
        return client;
    }
    
    /**
     * 获取默认客户端
     * 
     * @return 默认客户端实例
     * @throws OssException 默认客户端不存在
     */
    public OssClient getDefault() {
        return get(DEFAULT_CLIENT_NAME);
    }
    
    /**
     * 安全获取客户端（不抛出异常）
     * 
     * @param clientName 客户端名称
     * @return 客户端实例，如果不存在则返回 null
     */
    public OssClient getOrNull(String clientName) {
        return CLIENT_CACHE.get(clientName);
    }
    
    /**
     * 检查客户端是否存在
     * 
     * @param clientName 客户端名称
     * @return 是否存在
     */
    public boolean exists(String clientName) {
        return CLIENT_CACHE.containsKey(clientName);
    }
    
    /**
     * 获取所有客户端名称
     * 
     * @return 客户端名称集合（不可修改）
     */
    public Set<String> getClientNames() {
        return Set.copyOf(CLIENT_CACHE.keySet());
    }
    
    /**
     * 移除客户端
     * 
     * @param clientName 客户端名称
     * @return 被移除的客户端实例，如果不存在则返回 null
     */
    public OssClient remove(String clientName) {
        OssClient removed = CLIENT_CACHE.remove(clientName);
        if (removed != null) {
            log.info("移除 OSS 客户端: {}", clientName);
        }
        return removed;
    }
    
    /**
     * 清空所有客户端
     */
    public void clear() {
        int size = CLIENT_CACHE.size();
        CLIENT_CACHE.clear();
        log.info("清空所有 OSS 客户端，共 {} 个", size);
    }
    
    /**
     * 获取客户端数量
     * 
     * @return 客户端数量
     */
    public int size() {
        return CLIENT_CACHE.size();
    }
    
    /**
     * 检查是否为空
     * 
     * @return 是否为空
     */
    public boolean isEmpty() {
        return CLIENT_CACHE.isEmpty();
    }
    
    /**
     * 设置客户端（已废弃，请使用 register）
     * 
     * @param clientName 客户端名称
     * @param ossClient 客户端实例
     * @deprecated 使用 {@link #register(String, OssClient)} 替代
     */
    @Deprecated
    public void set(String clientName, OssClient ossClient) {
        register(clientName, ossClient);
    }
}

