package com.lambda.cloud.netty.protocol.reflection;

import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import java.lang.reflect.Field;

/**
 * 字段元数据
 * <p>
 * 封装字段的反射信息和注解信息
 * </p>
 *
 * @param field         字段反射信息
 * @param protocolField 协议字段注解
 * @param validation    验证注解
 *
 */
public record FieldMetadata(Field field, ProtocolField protocolField, ProtocolValidation validation) {

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getFieldName() {
        return field.getName();
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * 获取字段顺序
     *
     * @return 字段顺序
     */
    public int getOrder() {
        return protocolField.order();
    }

    /**
     * 获取字段长度
     *
     * @return 字段长度
     */
    public int getLength() {
        int length = protocolField.length();
        if (length == -1) {
            length = protocolField.type().getDefaultLength();
        }
        return length;
    }

    /**
     * 是否为可选字段
     *
     * @return true表示可选
     */
    public boolean isOptional() {
        return protocolField.optional();
    }

    /**
     * 是否有验证注解
     *
     * @return true表示有验证注解
     */
    public boolean hasValidation() {
        return validation != null;
    }
}
