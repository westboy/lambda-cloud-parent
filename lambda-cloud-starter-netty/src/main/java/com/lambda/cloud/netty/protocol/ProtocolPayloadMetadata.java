package com.lambda.cloud.netty.protocol;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressFBWarnings("EI_EXPOSE_REP")
public record ProtocolPayloadMetadata(
        Class<?> messageClass,
        ProtocolPayload protocolMessage,
        List<ProtocolFieldMetadata> fields,
        Map<Integer, ProtocolFieldMetadata> fieldMap,
        int totalLength) {

    /**
     * 创建消息元数据
     *
     * @param messageClass    消息类
     * @param protocolMessage 协议消息注解
     * @param fields          字段元数据列表
     * @return 消息元数据
     */
    public static ProtocolPayloadMetadata create(
            Class<?> messageClass, ProtocolPayload protocolMessage, List<ProtocolFieldMetadata> fields) {
        // 构建字段映射表
        Map<Integer, ProtocolFieldMetadata> fieldMap = new ConcurrentHashMap<>();
        int totalLength = 0;

        for (ProtocolFieldMetadata field : fields) {
            fieldMap.put(field.getOrder(), field);
            if (!field.isOptional()) {
                totalLength += field.getLength();
            }
        }

        return new ProtocolPayloadMetadata(messageClass, protocolMessage, fields, fieldMap, totalLength);
    }

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public String getFrameType() {
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
     * 获取消息名称
     *
     * @return 消息名称
     */
    public String getCrcAlgorithmName() {
        return protocolMessage.crcAlgorithm();
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
     * 是否为消息主体
     *
     * @return true 消息主体
     */
    public boolean isFrame() {
        return protocolMessage.isFrame();
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    @SuppressWarnings("unused")
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * 根据顺序获取字段（高效查找）
     *
     * @param order 字段顺序
     * @return 字段元数据
     */
    @SuppressWarnings("unused")
    public ProtocolFieldMetadata getFieldByOrder(int order) {
        return fieldMap.get(order);
    }

    /**
     * 计算指定字段顺序之后的数据偏移量。
     *
     * @param order 当前字段顺序
     * @return 从该字段之后到末尾的字节偏移总长度
     */
    public Integer getRemainingLengthAfter(int order) {
        return fields.stream()
                .filter(e -> e.getOrder() > order)
                .mapToInt(ProtocolFieldMetadata::getLength)
                .sum();
    }

    /**
     * 检查指定顺序之后是否存在长度未知的字段
     *
     * @param order 当前字段顺序
     * @return true 表示存在未知长度字段
     */
    public boolean hasUnknownLengthFieldsAfter(int order) {
        return fields.stream().filter(e -> e.getOrder() > order).anyMatch(e -> e.getLength() <= 0);
    }

    /**
     * 获取所有必填字段
     *
     * @return 必填字段列表
     */
    @SuppressWarnings("unused")
    public List<ProtocolFieldMetadata> getRequiredFields() {
        return fields.stream().filter(field -> !field.isOptional()).toList();
    }

    /**
     * 获取所有可选字段
     *
     * @return 可选字段列表
     */
    @SuppressWarnings("unused")
    public List<ProtocolFieldMetadata> getOptionalFields() {
        return fields.stream().filter(ProtocolFieldMetadata::isOptional).toList();
    }

    /**
     * 获取默认字符编码
     *
     * @return 默认字符编码
     */
    @SuppressWarnings("unused")
    public String getDefaultCharset() {
        return protocolMessage.defaultCharset();
    }
}
