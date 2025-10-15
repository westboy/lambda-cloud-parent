package com.lambda.cloud.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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

    public static ByteBuf acquire(int capacity) {
        return ALLOCATOR.buffer(capacity);
    }

    public static void release(ByteBuf buf) {
        if (buf != null && buf.refCnt() > 0) {
            buf.release();
        }
    }

    public static String getPoolStats() {
        return String.format("usedDirectMemory: %d", PlatformDependent.usedDirectMemory());
    }
}
