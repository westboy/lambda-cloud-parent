package com.lambda.cloud.netty.utils;

import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 转换器验证工具类
 * <p>
 * 提供通用的验证方法，消除重复的验证逻辑
 * </p>
 *
 * @author Jin
 */
public final class ByteBufferUtils {

    /**
     * 创建配置了字节序的ByteBuffer
     *
     * @param capacity      缓冲区容量
     * @param fieldMetadata 字段元数据
     * @return 配置了字节序的ByteBuffer
     */
    public static ByteBuffer createByteBuffer(int capacity, ProtocolFieldMetadata fieldMetadata) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(fieldMetadata.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return buffer;
    }
}
