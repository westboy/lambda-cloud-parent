package com.lambda.cloud.netty.protocol.core;

import io.netty.buffer.ByteBuf;

/**
 * 协议引擎核心接口
 * <p>
 * 定义协议处理的核心功能，支持双向转换和验证
 * </p>
 *
 * @param <T> 协议消息类型
 * @author Jin
 */
public interface ProtocolEngine<T> {

    /**
     * 从字节缓冲区解析协议消息
     *
     * @param byteBuf 字节缓冲区
     * @param messageClass 消息类型
     * @return 解析后的消息对象
     * @throws ProtocolException 协议解析异常
     */
    T parse(ByteBuf byteBuf, Class<T> messageClass) throws ProtocolException;

    /**
     * 将协议消息序列化到字节缓冲区
     *
     * @param message 消息对象
     * @param byteBuf 字节缓冲区
     * @throws ProtocolException 协议序列化异常
     */
    void serialize(T message, ByteBuf byteBuf) throws ProtocolException;

    /**
     * 验证协议消息
     *
     * @param message 消息对象
     * @return 验证结果
     */
    ValidationResult validate(T message);

    /**
     * 计算消息长度
     *
     * @param messageClass 消息类型
     * @return 消息长度（字节数）
     */
    int calculateLength(Class<?> messageClass);

    /**
     * 获取消息元数据
     *
     * @param messageClass 消息类型
     * @return 消息元数据
     */
    MessageMetadata getMetadata(Class<?> messageClass);

    /**
     * 验证结果
     */
    record ValidationResult(boolean valid, String message, String fieldName) {

        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message, null);
        }

        public static ValidationResult failure(String message, String fieldName) {
            return new ValidationResult(false, message, fieldName);
        }
    }
}
