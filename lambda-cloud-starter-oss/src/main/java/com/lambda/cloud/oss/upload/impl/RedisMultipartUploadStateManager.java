package com.lambda.cloud.oss.upload.impl;

import cn.hutool.json.JSONUtil;
import com.amazonaws.services.s3.model.PartETag;
import com.lambda.cloud.oss.upload.MultipartUploadStateManager;
import com.lambda.cloud.redis.helper.RedisHelper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Redis 的分片上传状态管理实现
 * 适用于分布式环境
 *
 * @author jpjoo
 */
@Slf4j
public class RedisMultipartUploadStateManager implements MultipartUploadStateManager {

    private static final String KEY_PREFIX = "oss:multipart:";
    private static final long DEFAULT_EXPIRE_HOURS = 24;

    private final RedisHelper redisHelper;

    public RedisMultipartUploadStateManager(RedisHelper redisHelper) {
        if (redisHelper == null) {
            throw new IllegalArgumentException("RedisHelper 不能为 null");
        }
        this.redisHelper = redisHelper;
        log.info("初始化 Redis 分片上传状态管理器");
    }

    @Override
    public void saveUploadId(String stateKey, String uploadId) {
        String key = buildKey(stateKey);
        redisHelper.hPut(key, "uploadId", uploadId);
        redisHelper.expire(key, DEFAULT_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("保存上传 ID: key={}, uploadId={}", stateKey, uploadId);
    }

    @Override
    public String getUploadId(String stateKey) {
        String uploadId = (String) redisHelper.hGet(buildKey(stateKey), "uploadId");
        log.debug("获取上传 ID: key={}, uploadId={}", stateKey, uploadId);
        return uploadId;
    }

    @Override
    public void savePartETags(String stateKey, List<PartETag> partETags) {
        String key = buildKey(stateKey);
        redisHelper.hPut(key, "partETags", JSONUtil.toJsonStr(partETags));
        redisHelper.expire(key, DEFAULT_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("保存分片标签: key={}, count={}", stateKey, partETags.size());
    }

    @Override
    public List<PartETag> getPartETags(String stateKey) {
        String json = (String) redisHelper.hGet(buildKey(stateKey), "partETags");
        if (json == null) {
            log.debug("获取分片标签: key={}, result=null", stateKey);
            return null;
        }
        List<PartETag> partETags = JSONUtil.toList(json, PartETag.class);
        log.debug("获取分片标签: key={}, count={}", stateKey, partETags.size());
        return partETags;
    }

    @Override
    public void deleteState(String stateKey) {
        redisHelper.delete(buildKey(stateKey));
        log.debug("删除上传状态: key={}", stateKey);
    }

    @Override
    public boolean exists(String stateKey) {
        boolean exists = redisHelper.hasKey(buildKey(stateKey));
        log.debug("检查状态存在: key={}, exists={}", stateKey, exists);
        return exists;
    }

    /**
     * 构建 Redis 键
     *
     * @param stateKey 状态键
     * @return Redis 键
     */
    private String buildKey(String stateKey) {
        return KEY_PREFIX + stateKey;
    }
}
