package com.lambda.cloud.netty.pool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

/**
 * ByteBuf对象池
 * <p>
 * 管理ByteBuf对象的创建和回收，提高内存使用效率，支持内存泄漏检测
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class ByteBufPool {

    private static final PooledByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;

    public static ByteBuf buffer() {
        return ALLOCATOR.buffer();
    }

    public static ByteBuf acquire(int capacity) {
        return ALLOCATOR.buffer(capacity);
    }

    public static void safeRelease(ByteBuf buf) {
        if (buf == null) return;
        try {
            int before = buf.refCnt();
            if (before > 0) {
                buf.release();
            }
        } catch (IllegalReferenceCountException e) {
            log.warn("Attempted double-release ByteBuf: {}", e.getMessage());
        }
    }

    public static String getPoolStats() {
        PooledByteBufAllocatorMetric m = ALLOCATOR.metric();
        return String.format(
                "usedDirectMemory=%d, normalCacheSize=%d, threadLocalCaches=%d",
                PlatformDependent.usedDirectMemory(), m.normalCacheSize(), m.numThreadLocalCaches());
    }
}
