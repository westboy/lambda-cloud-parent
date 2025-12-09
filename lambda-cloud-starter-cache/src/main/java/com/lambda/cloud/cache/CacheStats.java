package com.lambda.cloud.cache;

import lombok.Builder;
import lombok.Data;

/**
 * 缓存统计信息
 */
@Data
@Builder
public class CacheStats {

    /**
     * 缓存命中次数
     */
    private long hitCount;

    /**
     * 缓存未命中次数
     */
    private long missCount;

    /**
     * 缓存加载成功次数
     */
    private long loadSuccessCount;

    /**
     * 缓存加载失败次数
     */
    private long loadFailureCount;

    /**
     * 缓存加载总耗时(纳秒)
     */
    private long totalLoadTime;

    /**
     * 缓存驱逐次数
     */
    private long evictionCount;

    /**
     * 缓存大小
     */
    private long size;

    /**
     * 计算缓存命中率
     *
     * @return 命中率(0-1之间)
     */
    public double hitRate() {
        long totalRequests = hitCount + missCount;
        return totalRequests == 0 ? 0.0 : (double) hitCount / totalRequests;
    }

    /**
     * 计算缓存未命中率
     *
     * @return 未命中率(0-1之间)
     */
    public double missRate() {
        long totalRequests = hitCount + missCount;
        return totalRequests == 0 ? 0.0 : (double) missCount / totalRequests;
    }

    /**
     * 计算平均加载时间(毫秒)
     *
     * @return 平均加载时间
     */
    public double averageLoadPenalty() {
        long totalLoads = loadSuccessCount + loadFailureCount;
        return totalLoads == 0 ? 0.0 : (double) totalLoadTime / totalLoads / 1_000_000;
    }
}
