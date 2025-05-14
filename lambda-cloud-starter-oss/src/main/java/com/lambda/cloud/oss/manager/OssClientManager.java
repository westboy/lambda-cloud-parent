package com.lambda.cloud.oss.manager;

import com.lambda.cloud.oss.client.OssClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OssManager
 *
 * @author jpjoo
 */
public class OssClientManager {

    private static final Map<String, OssClient> CLIENT_CACHE = new ConcurrentHashMap<>();

    public void set(String clientName, OssClient ossClient) {
        CLIENT_CACHE.put(clientName, ossClient);
    }

    public OssClient get(String clientName) {
        return CLIENT_CACHE.get(clientName);
    }

}
