package com.lambda.cloud.netty.protocol.converter;

import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 数据类型转换器接口
 * <p>
 * 定义协议数据类型转换的统一标准
 * </p>
 *
 * @author Jin
 */
public interface DataTypeConverter {

    /**
     * 解析字节数据为对象
     *
     * @param buffer        字节缓冲区
     * @param length        读取长度
     * @param fieldMetadata 字段元数据
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 序列化对象为字节数据
     *
     * @param value         对象值
     * @param buffer        字节缓冲区
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 序列化异常
     */
    void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 从字符串解析对象（用于默认值）
     *
     * @param value         字符串值
     * @param fieldMetadata 字段元数据
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException;

    /**
     * 验证数据长度
     *
     * @param length        数据长度
     * @param fieldMetadata 字段元数据
     */
    default void validateLength(int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        Assert.isTrue(
                length == fieldMetadata.getLength(),
                "ASCII数据长度不匹配，期望: " + fieldMetadata.getLength() + ", 实际: " + length);
    }

    /**
     * 获取期望的数据长度
     *
     * @param fieldMetadata 字段元数据
     * @return 数据长度
     */
    default int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return fieldMetadata.getLength();
    }

    /**
     * 解析字节数据为对象（支持加密字段）
     * <p>
     * 如果字段标记为加密，将先解密再解析
     * </p>
     *
     * @param buffer            字节缓冲区
     * @param length            读取长度
     * @param fieldMetadata     字段元数据
     * @param encryptionService 加密服务（可选）
     * @return 解析后的对象
     * @throws ProtocolException 解析异常
     */
    default Object parseWithEncryption(
            ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata, EncryptionService encryptionService)
            throws ProtocolException {
        if (fieldMetadata.isEncryptedField() && encryptionService != null) {
            // 先读取数据
            byte[] data = new byte[length];
            buffer.readBytes(data);
            // 解密
            byte[] decryptedData = encryptionService.decrypt(data, fieldMetadata);
            // 包装解密后的数据
            ByteBuf decryptedBuf = Unpooled.wrappedBuffer(decryptedData);
            try {
                return parse(decryptedBuf, decryptedData.length, fieldMetadata);
            } finally {
                decryptedBuf.release();
            }
        } else {
            // 直接解析
            return parse(buffer, length, fieldMetadata);
        }
    }

    /**
     * 序列化对象为字节数据（支持加密字段）
     * <p>
     * 如果字段标记为加密，将先序列化再加密
     * </p>
     *
     * @param value             对象值
     * @param buffer            字节缓冲区
     * @param fieldMetadata     字段元数据
     * @param encryptionService 加密服务（可选）
     * @throws ProtocolException 序列化异常
     */
    default void serializeWithEncryption(
            Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata, EncryptionService encryptionService)
            throws ProtocolException {
        if (fieldMetadata.isEncryptedField() && encryptionService != null) {
            // 先序列化到临时缓冲区
            ByteBuf tempBuf = Unpooled.buffer();
            try {
                serialize(value, tempBuf, fieldMetadata);
                byte[] data = new byte[tempBuf.readableBytes()];
                tempBuf.readBytes(data);

                // 加密
                byte[] encryptedData = encryptionService.encrypt(data, fieldMetadata);
                // 写入加密后的数据
                buffer.writeBytes(encryptedData);
            } finally {
                tempBuf.release();
            }
        } else {
            // 直接序列化
            serialize(value, buffer, fieldMetadata);
        }
    }
}
