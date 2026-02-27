package com.lambda.cloud.oss.upload.impl;

import com.amazonaws.services.s3.model.PartETag;
import com.lambda.cloud.oss.upload.MultipartUploadStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于内存的分片上传状态管理实现
 * 适用于单机环境或测试环境
 *
 * <p>注意：此实现不适用于分布式环境，应用重启后数据会丢失</p>
 *
 * @author jpjoo
 */
@Slf4j
public class InMemoryMultipartUploadStateManager implements MultipartUploadStateManager {

    private final Map<String, UploadState> stateMap = new ConcurrentHashMap<>();

    public InMemoryMultipartUploadStateManager() {
        log.warn("使用内存分片上传状态管理器，不适用于分布式环境，应用重启后数据会丢失");
    }

    @Override
    public void saveUploadId(String stateKey, String uploadId) {
        stateMap.computeIfAbsent(stateKey, k -> new UploadState()).uploadId = uploadId;
        log.debug("保存上传 ID: key={}, uploadId={}", stateKey, uploadId);
    }

    @Override
    public String getUploadId(String stateKey) {
        UploadState state = stateMap.get(stateKey);
        String uploadId = state != null ? state.uploadId : null;
        log.debug("获取上传 ID: key={}, uploadId={}", stateKey, uploadId);
        return uploadId;
    }

    @Override
    public void savePartETags(String stateKey, List<PartETag> partETags) {
        // 创建副本以避免外部修改
        List<PartETag> copy = new ArrayList<>(partETags);
        stateMap.computeIfAbsent(stateKey, k -> new UploadState()).partETags = copy;
        log.debug("保存分片标签: key={}, count={}", stateKey, partETags.size());
    }

    @Override
    public List<PartETag> getPartETags(String stateKey) {
        UploadState state = stateMap.get(stateKey);
        List<PartETag> partETags = state != null ? state.partETags : null;
        if (partETags != null) {
            // 返回副本以避免外部修改
            partETags = new ArrayList<>(partETags);
            log.debug("获取分片标签: key={}, count={}", stateKey, partETags.size());
        } else {
            log.debug("获取分片标签: key={}, result=null", stateKey);
        }
        return partETags;
    }

    @Override
    public void deleteState(String stateKey) {
        stateMap.remove(stateKey);
        log.debug("删除上传状态: key={}", stateKey);
    }

    @Override
    public boolean exists(String stateKey) {
        boolean exists = stateMap.containsKey(stateKey);
        log.debug("检查状态存在: key={}, exists={}", stateKey, exists);
        return exists;
    }

    /**
     * 获取当前存储的状态数量
     *
     * @return 状态数量
     */
    public int size() {
        return stateMap.size();
    }

    /**
     * 清空所有状态
     */
    public void clear() {
        int size = stateMap.size();
        stateMap.clear();
        log.info("清空所有上传状态，共 {} 个", size);
    }

    /**
     * 上传状态内部类
     */
    private static class UploadState {
        String uploadId;
        List<PartETag> partETags;
    }
}
