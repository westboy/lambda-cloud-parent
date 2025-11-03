package com.lambda.cloud.netty.utils;

import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * 转换器验证工具类
 * <p>
 * 提供通用的验证方法，消除重复的验证逻辑
 * </p>
 *
 * @author Jin
 */
public final class ByteBufferUtils {
    private static final ThreadLocal<ByteBuffer> BUFFER_POOL = ThreadLocal.withInitial(() -> ByteBuffer.allocate(8));

    /**
     * 创建配置了字节序的ByteBuffer
     *
     * @param capacity      缓冲区容量
     * @param fieldMetadata 字段元数据
     * @return 配置了字节序的ByteBuffer
     */
    public static ByteBuffer createByteBuffer(int capacity, ProtocolFieldMetadata fieldMetadata) {
        ByteBuffer buffer;
        if (capacity <= 8) {
            // 使用线程本地缓存
            buffer = BUFFER_POOL.get();
            buffer.clear(); // 重置 position/limit
            if (buffer.capacity() < capacity) {
                // 不够用就分配新缓冲区
                buffer = ByteBuffer.allocate(capacity);
            } else {
                buffer.limit(capacity); // 设置 limit
            }
        } else {
            // 容量较大，直接分配
            buffer = ByteBuffer.allocate(capacity);
        }
        buffer.order(fieldMetadata.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return buffer;
    }

    public static byte[] addAllBytes(List<byte[]> bytes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (byte[] by : bytes) {
                outputStream.write(by);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
