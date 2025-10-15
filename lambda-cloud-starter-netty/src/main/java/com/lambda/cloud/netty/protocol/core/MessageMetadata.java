package com.lambda.cloud.netty.protocol.core;

import com.lambda.cloud.netty.protocol.annotation.ProtocolMessage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息元数据
 * <p>
 * 封装消息类的反射信息和注解信息，提供高效的字段访问
 * </p>
 *
 * @param messageClass    消息类
 * @param protocolMessage 协议消息注解
 * @param fields          字段元数据列表（按order排序）
 * @param fieldMap        字段映射表（按order索引）
 * @param totalLength     消息总长度
 * @author Jin
 */
public record MessageMetadata(
        Class<?> messageClass,
        ProtocolMessage protocolMessage,
        List<FieldMetadata> fields,
        Map<Integer, FieldMetadata> fieldMap,
        int totalLength) {

    /**
     * 创建消息元数据
     *
     * @param messageClass    消息类
     * @param protocolMessage 协议消息注解
     * @param fields          字段元数据列表
     * @return 消息元数据
     */
    public static MessageMetadata create(
            Class<?> messageClass, ProtocolMessage protocolMessage, List<FieldMetadata> fields) {
        // 构建字段映射表
        Map<Integer, FieldMetadata> fieldMap = new ConcurrentHashMap<>();
        int totalLength = 0;

        for (FieldMetadata field : fields) {
            fieldMap.put(field.getOrder(), field);
            if (!field.isOptional()) {
                totalLength += field.getLength();
            }
        }

        return new MessageMetadata(messageClass, protocolMessage, fields, fieldMap, totalLength);
    }

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public String getMessageType() {
        return protocolMessage.frameType();
    }

    /**
     * 获取消息名称
     *
     * @return 消息名称
     */
    public String getMessageName() {
        return protocolMessage.name();
    }

    /**
     * 获取消息描述
     *
     * @return 消息描述
     */
    public String getDescription() {
        return protocolMessage.description();
    }

    /**
     * 是否为严格模式
     *
     * @return true表示严格模式
     */
    public boolean isStrictMode() {
        return protocolMessage.strictMode();
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * 根据顺序获取字段（高效查找）
     *
     * @param order 字段顺序
     * @return 字段元数据
     */
    public FieldMetadata getFieldByOrder(int order) {
        return fieldMap.get(order);
    }

    /**
     * 获取所有必填字段
     *
     * @return 必填字段列表
     */
    public List<FieldMetadata> getRequiredFields() {
        return fields.stream().filter(field -> !field.isOptional()).toList();
    }

    /**
     * 获取所有可选字段
     *
     * @return 可选字段列表
     */
    public List<FieldMetadata> getOptionalFields() {
        return fields.stream().filter(FieldMetadata::isOptional).toList();
    }

    /**
     * 获取默认字节序
     *
     * @return true表示小端，false表示大端
     */
    public boolean getDefaultLittleEndian() {
        return protocolMessage.defaultLittleEndian();
    }

    /**
     * 获取默认字符编码
     *
     * @return 默认字符编码
     */
    public String getDefaultCharset() {
        return protocolMessage.defaultCharset();
    }
}
