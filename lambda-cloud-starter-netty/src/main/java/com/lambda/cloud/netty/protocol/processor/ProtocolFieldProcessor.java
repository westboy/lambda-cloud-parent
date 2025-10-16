package com.lambda.cloud.netty.protocol.processor;

import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFrameMetadata;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

/**
 * 字段处理器
 * <p>
 * 负责处理协议字段的解析和序列化逻辑，将复杂的字段处理逻辑从引擎中分离出来
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class ProtocolFieldProcessor {

    /**
     * 解析字段
     *
     * @param byteBuf       字节缓冲区
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @param frameMetadata   消息元数据
     * @param converter     数据类型转换器
     * @throws ProtocolException 解析异常
     */
    public void parseField(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolFrameMetadata frameMetadata,
            DataTypeConverter converter)
            throws ProtocolException {

        // 验证缓冲区数据
        if (!validateBufferData(byteBuf, fieldMetadata)) {
            handleInsufficientData(instance, fieldMetadata);
            return;
        }

        // 特殊处理复合字段
        if (fieldMetadata.isComposite() && fieldMetadata.getLength() == 0) {
            // 复合字段长度为0时，动态计算实际长度
            Object value = parseCompositeFieldWithDynamicLength(byteBuf, fieldMetadata, converter);
            setFieldValue(instance, fieldMetadata, value);
        } else {
            // 普通字段或长度固定的复合字段
            byte[] fieldData = readFieldData(byteBuf, fieldMetadata);
            Object value = convertFieldData(fieldData, fieldMetadata, converter);
            setFieldValue(instance, fieldMetadata, value);
        }
    }

    /**
     * 序列化字段
     *
     * @param instance      源实例
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @param frameMetadata   消息元数据
     * @param converter     数据类型转换器
     * @throws ProtocolException 序列化异常
     */
    public void serializeField(
            Object instance,
            ByteBuf byteBuf,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolFrameMetadata frameMetadata,
            DataTypeConverter converter)
            throws ProtocolException {
        // 获取字段值
        Object value = getFieldValue(instance, fieldMetadata);

        // 处理空值
        if (!handleNullValue(value, fieldMetadata)) {
            return;
        }

        // 转换并写入数据
        byte[] fieldData = convertToBytes(value, fieldMetadata, converter);
        writeFieldData(byteBuf, fieldData);
    }

    /**
     * 验证缓冲区数据是否足够
     *
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @return 是否有足够数据
     */
    private boolean validateBufferData(ByteBuf byteBuf, ProtocolFieldMetadata fieldMetadata) {
        return byteBuf.readableBytes() >= fieldMetadata.getLength();
    }

    /**
     * 处理数据不足的情况
     *
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 解析异常
     */
    private void handleInsufficientData(Object instance, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (fieldMetadata.isOptional()) {
            // 可选字段，使用默认值
            setDefaultValue(instance, fieldMetadata);
        } else {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.BUFFER_UNDERFLOW,
                    "缓冲区数据不足，字段: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName());
        }
    }

    /**
     * 读取字段数据
     *
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @return 字段数据
     */
    private byte[] readFieldData(ByteBuf byteBuf, ProtocolFieldMetadata fieldMetadata) {
        byte[] fieldData = new byte[fieldMetadata.getLength()];
        byteBuf.readBytes(fieldData);
        return fieldData;
    }

    /**
     * 转换字段数据
     *
     * @param fieldData     字段数据
     * @param fieldMetadata 字段元数据
     * @param converter     转换器
     * @return 转换后的值
     * @throws ProtocolException 转换异常
     */
    private Object convertFieldData(byte[] fieldData, ProtocolFieldMetadata fieldMetadata, DataTypeConverter converter)
            throws ProtocolException {
        try {
            return converter.parse(fieldData, fieldMetadata);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "字段数据转换失败: " + fieldMetadata.getFieldName(), e);
        }
    }

    /**
     * 设置字段值
     *
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @param value         字段值
     * @throws ProtocolException 设置异常
     */
    private void setFieldValue(Object instance, ProtocolFieldMetadata fieldMetadata, Object value)
            throws ProtocolException {
        try {
            fieldMetadata.setValue(instance, value);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "设置字段值失败: " + fieldMetadata.getFieldName(), e);
        }
    }

    /**
     * 获取字段值
     *
     * @param instance      源实例
     * @param fieldMetadata 字段元数据
     * @return 字段值
     * @throws ProtocolException 获取异常
     */
    private Object getFieldValue(Object instance, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            return fieldMetadata.getValue(instance);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR, "获取字段值失败: " + fieldMetadata.getFieldName(), e);
        }
    }

    /**
     * 处理空值
     *
     * @param value         字段值
     * @param fieldMetadata 字段元数据
     * @return 是否继续处理
     * @throws ProtocolException 验证异常
     */
    private boolean handleNullValue(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            if (fieldMetadata.isOptional()) {
                // 可选字段为空，跳过
                return false;
            } else {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.VALIDATION_ERROR,
                        "必填字段不能为空: " + fieldMetadata.getFieldName(),
                        fieldMetadata.getFieldName());
            }
        }
        return true;
    }

    /**
     * 转换为字节数组
     *
     * @param value         字段值
     * @param fieldMetadata 字段元数据
     * @param converter     转换器
     * @return 字节数组
     * @throws ProtocolException 转换异常
     */
    private byte[] convertToBytes(Object value, ProtocolFieldMetadata fieldMetadata, DataTypeConverter converter)
            throws ProtocolException {
        try {
            return converter.serialize(value, fieldMetadata);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR, "字段数据序列化失败: " + fieldMetadata.getFieldName(), e);
        }
    }

    /**
     * 写入字段数据
     *
     * @param byteBuf   字节缓冲区
     * @param fieldData 字段数据
     */
    private void writeFieldData(ByteBuf byteBuf, byte[] fieldData) {
        byteBuf.writeBytes(fieldData);
    }

    /**
     * 设置默认值
     *
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 设置异常
     */
    private void setDefaultValue(Object instance, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        String defaultValue = fieldMetadata.getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            try {
                // 这里需要获取转换器，但为了保持方法简洁，暂时使用简单的处理方式
                // 在实际使用中，可以通过依赖注入或工厂模式获取转换器
                log.debug("设置默认值: {} = {}", fieldMetadata.getFieldName(), defaultValue);
                // TODO: 实现默认值设置逻辑
            } catch (Exception e) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.PARSE_ERROR, "设置默认值失败: " + fieldMetadata.getFieldName(), e);
            }
        }
    }

    /**
     * 解析长度为0的复合字段，动态计算实际长度
     *
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @param converter     转换器
     * @return 解析后的复合对象
     * @throws ProtocolException 解析异常
     */
    private Object parseCompositeFieldWithDynamicLength(
            ByteBuf byteBuf, ProtocolFieldMetadata fieldMetadata, DataTypeConverter converter)
            throws ProtocolException {
        try {
            // 获取复合字段的目标类型
            Class<?> targetType = fieldMetadata.getFieldType();

            // 计算复合字段的实际长度
            int actualLength = calculateCompositeFieldLength(targetType);

            // 读取实际长度的数据
            byte[] fieldData = new byte[actualLength];
            byteBuf.readBytes(fieldData);

            // 使用转换器解析数据
            return converter.parse(fieldData, fieldMetadata);

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "动态解析复合字段失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 计算复合字段的实际长度
     *
     * @param compositeType 复合字段类型
     * @return 实际长度
     * @throws ProtocolException 计算异常
     */
    private int calculateCompositeFieldLength(Class<?> compositeType) throws ProtocolException {
        try {
            int totalLength = 0;
            Field[] fields = compositeType.getDeclaredFields();

            for (Field field : fields) {
                ProtocolField protocolField = field.getAnnotation(ProtocolField.class);
                if (protocolField != null) {
                    totalLength += protocolField.length();
                }
            }

            log.debug("计算复合字段长度: {} = {}", compositeType.getSimpleName(), totalLength);
            return totalLength;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "计算复合字段长度失败: " + compositeType.getSimpleName(), e);
        }
    }
}
