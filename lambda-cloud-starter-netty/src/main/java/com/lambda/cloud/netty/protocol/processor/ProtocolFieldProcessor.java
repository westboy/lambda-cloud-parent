package com.lambda.cloud.netty.protocol.processor;

import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.ProtocolPayloadMetadata;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import com.lambda.cloud.netty.protocol.message.ProtocolMessage;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<Class<?>, Integer> compositeFieldLengthCache = new ConcurrentHashMap<>();
    private final ProtocolPayloadRegistry protocolPayloadRegistry = new ProtocolPayloadRegistry();
    private final EncryptionService encryptionService;

    /**
     * 构造函数（支持加密）
     *
     * @param encryptionService 加密服务
     */
    public ProtocolFieldProcessor(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        if (encryptionService != null) {
            log.info("字段处理器已启用加密支持，算法: {}", encryptionService.getAlgorithmName());
        }
    }

    /**
     * 解析字段
     *
     * @param byteBuf       字节缓冲区
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @param frameMetadata 消息元数据
     * @param converter     数据类型转换器
     * @throws ProtocolException 解析异常
     */
    public void parseField(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata frameMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException, IOException {

        // 验证缓冲区数据
        if (!validateBufferData(byteBuf, fieldMetadata)) {
            handleInsufficientData(instance, fieldMetadata);
            return;
        }

        // 特殊处理 List 字段
        if (fieldMetadata.isList()) {
            // List 字段需要特殊处理，计算实际需要读取的数据长度
            Object value =
                    parseListField(byteBuf, instance, fieldMetadata, frameMetadata, converter, isEncryptionEnabled);
            setFieldValue(instance, fieldMetadata, value);
        } else if (fieldMetadata.isComposite() && fieldMetadata.getLength() == 0) {
            // 复合字段长度为0时，动态计算实际长度
            Object value = parseCompositeFieldWithDynamicLength(
                    byteBuf, instance, fieldMetadata, frameMetadata, converter, isEncryptionEnabled);
            setFieldValue(instance, fieldMetadata, value);
        } else {
            // 普通字段或长度固定复合字段
            Object value =
                    convertFieldData(byteBuf, fieldMetadata.getLength(), fieldMetadata, converter, isEncryptionEnabled);
            setFieldValue(instance, fieldMetadata, value);
        }
    }

    /**
     * 序列化字段
     *
     * @param instance      源实例
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @param frameMetadata 消息元数据
     * @param converter     数据类型转换器
     * @throws ProtocolException 序列化异常
     */
    @SuppressWarnings("unused")
    public void serializeField(
            Object instance,
            ByteBuf byteBuf,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata frameMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException {
        // 获取字段值
        Object value = getFieldValue(instance, fieldMetadata);

        // 处理空值
        if (!handleNullValue(value, fieldMetadata)) {
            return;
        }

        // 转换并写入数据
        serializeValue(value, byteBuf, fieldMetadata, converter, isEncryptionEnabled);
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
     * 转换字段数据
     *
     * @param byteBuf       字节缓冲区
     * @param length        读取长度
     * @param fieldMetadata 字段元数据
     * @param converter     转换器
     * @return 转换后的值
     * @throws ProtocolException 转换异常
     */
    private Object convertFieldData(
            ByteBuf byteBuf,
            int length,
            ProtocolFieldMetadata fieldMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException {
        try {
            // 检查是否需要解密（需要同时满足：字段标记为加密 + 已启用加密控制 + 存在加密服务）
            if (isEncryptionEnabled && fieldMetadata.isEncryptedField() && encryptionService != null) {
                if (log.isDebugEnabled()) {
                    log.debug("解密字段数据: {}, 长度: {}", fieldMetadata.getFieldName(), length);
                }
                return converter.parseWithEncryption(byteBuf, length, fieldMetadata, encryptionService);
            } else {
                return converter.parse(byteBuf, length, fieldMetadata);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段数据转换失败: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName(),
                    e);
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
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "设置字段值失败: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName(),
                    e);
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
                    ProtocolException.ErrorCode.SERIALIZE_ERROR, "获取字段值失败!", fieldMetadata.getFieldName(), e);
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
     * 序列化字段值
     *
     * @param value         字段值
     * @param buffer        字节缓冲区
     * @param fieldMetadata 字段元数据
     * @param converter     转换器
     * @throws ProtocolException 转换异常
     */
    private void serializeValue(
            Object value,
            ByteBuf buffer,
            ProtocolFieldMetadata fieldMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException {

        try {
            if (isEncryptionEnabled && fieldMetadata.isEncryptedField() && encryptionService != null) {
                converter.serializeWithEncryption(value, buffer, fieldMetadata, encryptionService);
            } else {
                converter.serialize(value, buffer, fieldMetadata);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "字段数据序列化失败: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 设置默认值
     *
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 设置异常
     */
    @SuppressWarnings("unused")
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
                        ProtocolException.ErrorCode.PARSE_ERROR,
                        "设置默认值失败: " + fieldMetadata.getFieldName(),
                        fieldMetadata.getFieldName(),
                        e);
            }
        }
    }

    /**
     * 解析长度为0的复合字段，动态计算实际长度
     *
     * @param byteBuf       字节缓冲区
     * @param instance
     * @param fieldMetadata 字段元数据
     * @param frameMetadata 元数据
     * @param converter     转换器
     * @return 解析后的复合对象
     * @throws ProtocolException 解析异常
     */
    private Object parseCompositeFieldWithDynamicLength(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata frameMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException {
        try {
            Class<?> targetType = fieldMetadata.getFieldType();
            if (fieldMetadata.isPayload() && instance instanceof ProtocolMessage protocolMessage) {
                Class<?> protocolMessageType =
                        ProtocolPayloadRegistry.getProtocolMessage(protocolMessage.getFrameType());
                if (protocolMessageType != null) {
                    targetType = protocolMessageType;
                    fieldMetadata.extParam().put("CompositeType", protocolMessageType);
                }
            }
            if (isEncryptionEnabled && fieldMetadata.isEncryptedField() && encryptionService != null) {
                // 校验后续字段是否存在动态长度，如果存在则无法准确计算当前字段长度
                if (frameMetadata.hasUnknownLengthFieldsAfter(fieldMetadata.getOrder())) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.PARSE_ERROR,
                            "动态解析复合字段失败: " + fieldMetadata.getFieldName() + ", 原因: 后续存在未知长度字段，无法确定边界",
                            fieldMetadata.getFieldName());
                }

                // 加密字段的长度
                int remaining =
                        byteBuf.readableBytes() - frameMetadata.getRemainingLengthAfter(fieldMetadata.getOrder());
                Assert.isTrue(
                        remaining > 0,
                        "动态解析复合字段失败: " + fieldMetadata.getFieldName() + ", 原因: " + "计算字段长度异常，使用可读字节长度:"
                                + byteBuf.readableBytes());
                // 读取实际长度的数据
                return converter.parseWithEncryption(byteBuf, remaining, fieldMetadata, encryptionService);
            } else {
                int actualLength;
                if (fieldMetadata.isList()) {
                    int elementLength = calculateCompositeFieldLength(targetType);
                    int elementSize = fieldMetadata.getListElementSize();
                    String elementSizeField = fieldMetadata.getListElementSizeField();
                    if ((elementSize <= 0) && (elementSizeField != null) && (!elementSizeField.isBlank())) {
                        try {
                            Field field = instance.getClass().getDeclaredField(elementSizeField);
                            field.setAccessible(true);
                            Object value = field.get(instance);
                            if (value instanceof Number number) {
                                elementSize = number.intValue();
                            }
                        } catch (Exception e) {
                            throw new ProtocolException(
                                    ProtocolException.ErrorCode.PARSE_ERROR,
                                    "读取List元素数量字段失败: " + fieldMetadata.getFieldName(),
                                    fieldMetadata.getFieldName(),
                                    e);
                        }
                    }
                    if (elementSize <= 0) {
                        if (frameMetadata.hasUnknownLengthFieldsAfter(fieldMetadata.getOrder())) {
                            throw new ProtocolException(
                                    ProtocolException.ErrorCode.PARSE_ERROR,
                                    "无法确定List字段长度: " + fieldMetadata.getFieldName(),
                                    fieldMetadata.getFieldName());
                        }
                        int remaining =
                                byteBuf.readableBytes() - frameMetadata.getRemainingLengthAfter(fieldMetadata.getOrder());
                        if (remaining <= 0) {
                            return null;
                        }
                        if (remaining % elementLength != 0) {
                            throw new ProtocolException(
                                    ProtocolException.ErrorCode.PARSE_ERROR,
                                    "List字段长度无法整除元素长度: " + fieldMetadata.getFieldName(),
                                    fieldMetadata.getFieldName());
                        }
                        elementSize = remaining / elementLength;
                    }
                    actualLength = elementLength * elementSize;
                    fieldMetadata.extParam().put("CompositeType", targetType);
                    fieldMetadata.extParam().put("ListElementLength", elementLength);
                } else if (fieldMetadata.isPayload() && fieldMetadata.getLength() == 0) {
                    if (frameMetadata.hasUnknownLengthFieldsAfter(fieldMetadata.getOrder())) {
                        throw new ProtocolException(
                                ProtocolException.ErrorCode.PARSE_ERROR,
                                "动态解析Payload失败: " + fieldMetadata.getFieldName(),
                                fieldMetadata.getFieldName());
                    }
                    actualLength = byteBuf.readableBytes() - frameMetadata.getRemainingLengthAfter(fieldMetadata.getOrder());
                } else {
                    actualLength = calculateCompositeFieldLength(targetType);
                }
                // 读取实际长度的数据
                return converter.parse(byteBuf, actualLength, fieldMetadata);
            }

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
            return compositeFieldLengthCache.computeIfAbsent(compositeType, clazz -> {
                int totalLength = 0;
                Field[] fields = compositeType.getDeclaredFields();
                for (Field field : fields) {
                    ProtocolField protocolField = field.getAnnotation(ProtocolField.class);
                    if (protocolField != null) {
                        if (protocolField.listElementSize() > 0 && protocolField.dataType() == ProtocolDataType.LIST) {
                            totalLength += (protocolField.listElementSize() * protocolField.length());
                        } else {
                            totalLength += protocolField.length();
                        }
                    }
                }
                log.debug("计算复合字段长度: {} = {}", compositeType.getSimpleName(), totalLength);
                return totalLength;
            });
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "计算复合字段长度失败: " + compositeType.getSimpleName(), null, e);
        }
    }

    /**
     * 解析List字段
     *
     * @param byteBuf             字节缓冲区
     * @param instance
     * @param fieldMetadata       字段元数据
     * @param frameMetadata       帧元数据
     * @param converter           转换器
     * @param isEncryptionEnabled 是否启用加密
     * @param outputStream        原文
     * @return 解析后的 List 对象
     * @throws ProtocolException 解析异常
     */
    private Object parseListField(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata frameMetadata,
            DataTypeConverter converter,
            boolean isEncryptionEnabled)
            throws ProtocolException {

        log.info(
                "解析List字段: fieldName={}, length={}, listElementSize={}, isComposite={}",
                fieldMetadata.getFieldName(),
                fieldMetadata.getLength(),
                fieldMetadata.getListElementSize(),
                fieldMetadata.isComposite());

        if (fieldMetadata.isComposite()) {
            return parseCompositeFieldWithDynamicLength(
                    byteBuf, instance, fieldMetadata, frameMetadata, converter, isEncryptionEnabled);
        } else {
            // 计算 List 字段需要读取的总字节数
            int totalBytes = calculateListFieldLength(byteBuf, instance, fieldMetadata, frameMetadata);
            log.info("计算得到的总字节数: {}", totalBytes);

            // 使用 ListConverter 解析数据
            Object result = convertFieldData(byteBuf, totalBytes, fieldMetadata, converter, isEncryptionEnabled);
            log.info("解析结果类型: {}, 值: {}", result != null ? result.getClass().getName() : "null", result);
            return result;
        }
    }

    /**
     * 计算 List字段需要读取的总字节数
     *
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @return 需要读取的字节数
     */
    @SuppressWarnings("unused")
    private int calculateListFieldLength(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata frameMetadata)
            throws ProtocolException {
        int listLength = fieldMetadata.getLength();
        int elementSize = fieldMetadata.getListElementSize();
        String elementSizeField = fieldMetadata.getListElementSizeField();
        if ((elementSize <= 0) && (elementSizeField != null) && (!elementSizeField.isBlank())) {
            try {
                Field field = instance.getClass().getDeclaredField(elementSizeField);
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value instanceof Number number) {
                    elementSize = number.intValue();
                }
            } catch (Exception e) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.PARSE_ERROR,
                        "读取List元素数量字段失败: " + fieldMetadata.getFieldName(),
                        fieldMetadata.getFieldName(),
                        e);
            }
        }
        // 如果listLength > 0，表示固定长度的List
        if (elementSize > 0) {
            return listLength * elementSize;
        } else {
            if (frameMetadata.hasUnknownLengthFieldsAfter(fieldMetadata.getOrder())) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.PARSE_ERROR,
                        "无法确定List字段长度: " + fieldMetadata.getFieldName(),
                        fieldMetadata.getFieldName());
            }
            int remaining = byteBuf.readableBytes() - frameMetadata.getRemainingLengthAfter(fieldMetadata.getOrder());
            if (remaining <= 0) {
                return 0;
            }
            return remaining;
        }
    }
}
