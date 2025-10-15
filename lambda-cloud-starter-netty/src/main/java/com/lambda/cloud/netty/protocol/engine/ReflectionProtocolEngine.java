package com.lambda.cloud.netty.protocol.engine;

import com.lambda.cloud.netty.protocol.ProtocolEngine;
import com.lambda.cloud.netty.protocol.ProtocolException;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolMessage;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.cache.CacheManager;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.core.*;
import com.lambda.cloud.netty.protocol.processor.FieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationEngine;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 反射协议引擎
 * <p>
 * 基于反射的高性能协议解析引擎，支持缓存和优化
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class ReflectionProtocolEngine implements ProtocolEngine<Object> {

    /**
     * 元数据缓存
     */
    private final Map<Class<?>, MessageMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * 数据类型转换器工厂
     */
    private final DataTypeConverterFactory converterFactory = new DataTypeConverterFactory();

    /**
     * 验证引擎
     */
    private final ValidationEngine validationEngine = new ValidationEngine();

    /**
     * 字段缓存管理器
     */
    private final CacheManager<String, List<Field>> fieldCache = new CacheManager<>(1000);

    /**
     * 转换器缓存管理器
     */
    private final CacheManager<ProtocolDataType, DataTypeConverter> converterCache = new CacheManager<>(100);

    /**
     * 字段处理器
     */
    private final FieldProcessor fieldProcessor = new FieldProcessor();

    @Override
    public Object parse(ByteBuf byteBuf, Class<Object> messageClass) throws ProtocolException {
        MessageMetadata metadata = getMetadata(messageClass);

        try {
            Object instance = messageClass.getDeclaredConstructor().newInstance();

            for (FieldMetadata fieldMetadata : metadata.fields()) {
                parseField(byteBuf, instance, fieldMetadata, metadata);
            }

            return instance;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "解析消息失败: " + messageClass.getSimpleName());
        }
    }

    @Override
    public void serialize(Object message, ByteBuf byteBuf) throws ProtocolException {
        MessageMetadata metadata = getMetadata(message.getClass());

        try {
            for (FieldMetadata fieldMetadata : metadata.fields()) {
                serializeField(message, byteBuf, fieldMetadata, metadata);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化消息失败: " + message.getClass().getSimpleName());
        }
    }

    @Override
    public ValidationResult validate(Object message) {
        try {
            MessageMetadata metadata = getMetadata(message.getClass());
            return validationEngine.validate(message, metadata);
        } catch (Exception e) {
            log.error("验证消息时发生异常", e);
            return ValidationResult.failure("验证过程中发生异常: " + e.getMessage());
        }
    }

    @Override
    public int calculateLength(Class<?> messageClass) {
        MessageMetadata metadata = getMetadata(messageClass);
        return metadata.totalLength();
    }

    @Override
    public MessageMetadata getMetadata(Class<?> messageClass) {
        return metadataCache.computeIfAbsent(messageClass, this::buildMetadata);
    }

    /**
     * 解析字段
     *
     * @param byteBuf       字节缓冲区
     * @param instance      目标实例
     * @param fieldMetadata 字段元数据
     * @param msgMetadata   消息元数据
     * @throws ProtocolException 解析异常
     */
    private void parseField(ByteBuf byteBuf, Object instance, FieldMetadata fieldMetadata, MessageMetadata msgMetadata)
            throws ProtocolException {

        // 使用字段处理器处理解析逻辑
        DataTypeConverter converter = getConverterFromCache(fieldMetadata.getDataType());
        fieldProcessor.parseField(byteBuf, instance, fieldMetadata, msgMetadata, converter);
    }

    /**
     * 序列化字段
     *
     * @param instance      源实例
     * @param byteBuf       字节缓冲区
     * @param fieldMetadata 字段元数据
     * @param msgMetadata   消息元数据
     * @throws ProtocolException 序列化异常
     */
    private void serializeField(
            Object instance, ByteBuf byteBuf, FieldMetadata fieldMetadata, MessageMetadata msgMetadata)
            throws ProtocolException {

        // 使用字段处理器处理序列化逻辑
        DataTypeConverter converter = getConverterFromCache(fieldMetadata.getDataType());
        fieldProcessor.serializeField(instance, byteBuf, fieldMetadata, msgMetadata, converter);
    }

    /**
     * 构建消息元数据
     *
     * @param messageClass 消息类
     * @return 消息元数据
     */
    private MessageMetadata buildMetadata(Class<?> messageClass) {
        ProtocolMessage protocolMessage = messageClass.getAnnotation(ProtocolMessage.class);
        if (protocolMessage == null) {
            throw new IllegalArgumentException("类必须标注 @ProtocolMessage 注解: " + messageClass.getName());
        }

        List<FieldMetadata> fields = new ArrayList<>();

        // 收集所有标注了 @ProtocolField 的字段
        for (Field field : getAllFields(messageClass)) {
            ProtocolField protocolField = field.getAnnotation(ProtocolField.class);
            if (protocolField != null) {
                ProtocolValidation validation = field.getAnnotation(ProtocolValidation.class);
                fields.add(new FieldMetadata(field, protocolField, validation));
            }
        }

        // 按 order 排序
        fields.sort(Comparator.comparingInt(FieldMetadata::getOrder));

        return MessageMetadata.create(messageClass, protocolMessage, fields);
    }

    /**
     * 获取类的所有字段（包括父类）
     *
     * @param clazz 类
     * @return 字段列表
     */
    private List<Field> getAllFields(Class<?> clazz) {
        String className = clazz.getName();

        // 尝试从缓存获取
        List<Field> cachedFields = fieldCache.get(className);
        if (cachedFields != null) {
            return cachedFields;
        }

        // 构建字段列表
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        // 缓存结果
        fieldCache.put(className, fields);

        return fields;
    }

    /**
     * 从缓存获取转换器
     *
     * @param dataType 数据类型
     * @return 转换器
     */
    private DataTypeConverter getConverterFromCache(ProtocolDataType dataType) {
        DataTypeConverter converter = converterCache.get(dataType);
        if (converter == null) {
            converter = converterFactory.getConverter(dataType);
            converterCache.put(dataType, converter);
        }
        return converter;
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public String getCacheStats() {
        return "ReflectionProtocolEngine Cache Statistics:\n" + "Field Cache: "
                + fieldCache.getStats() + "\n" + "Converter Cache: "
                + converterCache.getStats();
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        fieldCache.clear();
        converterCache.clear();
    }
}
