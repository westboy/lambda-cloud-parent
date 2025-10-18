package com.lambda.cloud.netty.utils;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;

/**
 * 异常工具类
 * <p>
 * 提供通用的异常创建方法，消除重复的异常逻辑
 * </p>
 *
 * @author Jin
 */
public final class ExceptionUtils {

    /**
     * 创建解析错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @param cause         原因异常
     * @return ProtocolException实例
     */
    public static ProtocolException createParseException(
            String message, ProtocolFieldMetadata fieldMetadata, Throwable cause) {
        return new ProtocolException(
                ProtocolException.ErrorCode.PARSE_ERROR,
                message,
                fieldMetadata != null ? fieldMetadata.getFieldName() : null,
                cause);
    }

    /**
     * 创建解析错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @return ProtocolException实例
     */
    public static ProtocolException createParseException(String message, ProtocolFieldMetadata fieldMetadata) {
        return createParseException(message, fieldMetadata, null);
    }

    /**
     * 创建序列化错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @param cause         原因异常
     * @return ProtocolException实例
     */
    public static ProtocolException createSerializeException(
            String message, ProtocolFieldMetadata fieldMetadata, Throwable cause) {
        return new ProtocolException(
                ProtocolException.ErrorCode.SERIALIZE_ERROR,
                message,
                fieldMetadata != null ? fieldMetadata.getFieldName() : null,
                cause);
    }

    /**
     * 创建序列化错误异常
     *
     * @param message       错误消息
     * @param fieldMetadata 字段元数据
     * @return ProtocolException实例
     */
    public static ProtocolException createSerializeException(String message, ProtocolFieldMetadata fieldMetadata) {
        return createSerializeException(message, fieldMetadata, null);
    }
}
