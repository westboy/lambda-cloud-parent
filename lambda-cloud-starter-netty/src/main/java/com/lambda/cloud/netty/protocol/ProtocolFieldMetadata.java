package com.lambda.cloud.netty.protocol;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.annotation.PaddingDirection;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;

/**
 * 字段元数据
 * <p>
 * 封装字段的反射信息和注解信息，提供高效的字段访问
 * </p>
 *
 * @param fieldAccessor 字段反射信息
 * @param protocolField 协议字段注解
 * @param validation    验证注解
 * @author Jin
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record ProtocolFieldMetadata(
        FieldAccessor fieldAccessor,
        ProtocolField protocolField,
        ProtocolValidation validation,
        Map<String, Object> extParam) {

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getFieldName() {
        return fieldAccessor.getFieldName();
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public Class<?> getFieldType() {
        return fieldAccessor.getFieldType();
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
            length = protocolField.dataType().getDefaultLength();
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
     * @return true 表示有验证注解
     */
    public boolean hasValidation() {
        return validation != null;
    }

    /**
     * 是否为复合字段
     *
     * @return true 表示复合字段
     */
    public boolean isComposite() {
        return protocolField.composite();
    }

    /**
     * 获取数据类型
     *
     * @return 数据类型
     */
    public ProtocolDataType getDataType() {
        return protocolField.dataType();
    }

    /**
     * 是否为小端字节序
     *
     * @return true 表示小端
     */
    public boolean isLittleEndian() {
        return protocolField.littleEndian();
    }

    /**
     * 获取字符编码
     *
     * @return 字符编码
     */
    public String getCharset() {
        return protocolField.charset();
    }

    /**
     * 获取默认值
     *
     * @return 默认值
     */
    public String getDefaultValue() {
        return protocolField.defaultValue();
    }

    /**
     * 获取字段的精度（小数位数）
     *
     * @return 小数位数
     */
    public int getPrecision() {
        return protocolField.precision();
    }

    /**
     * 获取填充方向
     *
     * @return 填充方向
     */
    public PaddingDirection getPaddingDirection() {
        return protocolField.padding();
    }

    /**
     * 获取填充字符
     *
     * @return 填充字符
     */
    public String getPaddingChar() {
        return protocolField.paddingChar();
    }

    /**
     * 获取字段描述
     *
     * @return 字段描述
     */
    public String getDescription() {
        return protocolField.description();
    }

    /**
     * 是否为CRC校验字段（参与CRC计算的字段）
     *
     * @return true 表示CRC校验字段
     */
    public boolean isComputed() {
        return protocolField.computed();
    }

    /**
     * 是否 Payload
     *
     * @return true 是Payload
     */
    public boolean isPayload() {
        return protocolField.payload();
    }

    /**
     * 是否为CRC字段（存储CRC值的字段）
     *
     * @return true 表示CRC字段
     */
    public boolean isCrcField() {
        return protocolField.crcFiled();
    }

    public boolean isLengthFiled() {
        return protocolField.lengthFiled();
    }

    /**
     * 是否为加密控制字段
     * 标识该字段是否用于控制加密功能的开关
     *
     * @return true 表示加密控制字段
     */
    public boolean isEncryptionKey() {
        return protocolField.encryptedKey();
    }

    /**
     * 是否为加密数据字段
     * 标识该字段是否为需要加密的数据字段
     * 注意：实际是否加密还需要检查加密控制字段的值
     *
     * @return true 表示加密数据字段
     */
    public boolean isEncryptedField() {
        return protocolField.encryptedField();
    }

    /**
     * 设置字段值
     *
     * @param target 目标对象
     * @param value  字段值
     * @throws ProtocolException 设置失败时抛出异常
     */
    public void setValue(Object target, Object value) throws ProtocolException {
        try {
            fieldAccessor.setValue(target, value);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.REFLECTION_ERROR, "无法设置字段值: " + getFieldName(), getFieldName(), e);
        }
    }

    /**
     * 获取字段值
     *
     * @param target 目标对象
     * @return 字段值
     * @throws ProtocolException 获取失败时抛出异常
     */
    public Object getValue(Object target) throws ProtocolException {
        try {
            return fieldAccessor.getValue(target);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.REFLECTION_ERROR, "无法获取字段值: " + getFieldName(), getFieldName(), e);
        }
    }

    /**
     * 是否为List字段
     *
     * @return true 表示List字段
     */
    public boolean isList() {
        return protocolField.dataType() == ProtocolDataType.LIST
                || List.class.isAssignableFrom(fieldAccessor.getFieldType());
    }

    /**
     * 获取List元素数据类型
     *
     * @return List元素的数据类型
     */
    public ProtocolDataType getListElementDataType() {
        return protocolField.listElementType();
    }

    /**
     * 获取List元素个数
     *
     * @return List元素长度
     */
    public int getListElementSize() {
        return protocolField.listElementSize();
    }
}
