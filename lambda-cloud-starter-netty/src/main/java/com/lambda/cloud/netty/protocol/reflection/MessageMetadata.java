package com.lambda.cloud.netty.protocol.reflection;

import com.lambda.cloud.netty.protocol.annotation.ProtocolMessage;
import java.util.List;

/**
 * 消息元数据
 * <p>
 * 封装消息类的反射信息和注解信息
 * </p>
 *
 * @param messageClass    消息类
 * @param protocolMessage 协议消息注解
 * @param fields          字段元数据列表（按order排序）
 */
public record MessageMetadata(Class<?> messageClass, ProtocolMessage protocolMessage, List<FieldMetadata> fields) {

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public String getMessageType() {
        return protocolMessage.messageType();
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
     * 根据顺序获取字段
     *
     * @param order 字段顺序
     * @return 字段元数据
     */
    public FieldMetadata getFieldByOrder(int order) {
        return fields.stream()
                .filter(field -> field.getOrder() == order)
                .findFirst()
                .orElse(null);
    }
}
