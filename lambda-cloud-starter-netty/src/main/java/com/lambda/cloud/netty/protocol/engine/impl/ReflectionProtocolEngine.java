package com.lambda.cloud.netty.protocol.engine.impl;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.cache.CacheManager;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.meta.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.meta.ProtocolFrameMetadata;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationEngine;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
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
    private final Map<Class<?>, ProtocolFrameMetadata> metadataCache = new ConcurrentHashMap<>();

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
    private final ProtocolFieldProcessor protocolFieldProcessor = new ProtocolFieldProcessor();

    @Override
    public Object parse(ByteBuf byteBuf, Class<Object> messageClass) throws ProtocolException {
        ProtocolFrameMetadata metadata = getMetadata(messageClass);
        try {
            printLog("帧解析", metadata);
            Object instance = messageClass.getDeclaredConstructor().newInstance();
            for (ProtocolFieldMetadata fieldMetadata : metadata.fields()) {
                parseField(byteBuf, instance, fieldMetadata, metadata);
            }
            return instance;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "解析消息失败: " + messageClass.getSimpleName(), e);
        }
    }

    @Override
    public void serialize(Object message, ByteBuf byteBuf) throws ProtocolException {
        ProtocolFrameMetadata metadata = getMetadata(message.getClass());
        try {
            printLog("序列化", metadata);
            for (ProtocolFieldMetadata fieldMetadata : metadata.fields()) {
                serializeField(message, byteBuf, fieldMetadata, metadata);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化消息失败: " + message.getClass().getSimpleName(),
                    e);
        }
    }

    @Override
    public ValidationResult validate(Object message) {
        try {
            ProtocolFrameMetadata metadata = getMetadata(message.getClass());
            return validationEngine.validate(message, metadata);
        } catch (Exception e) {
            log.error("验证消息时发生异常", e);
            return ValidationResult.failure("验证过程中发生异常: " + e.getMessage());
        }
    }

    @Override
    public int calculateLength(Class<?> messageClass) {
        ProtocolFrameMetadata metadata = getMetadata(messageClass);
        return metadata.totalLength();
    }

    @Override
    public ProtocolFrameMetadata getMetadata(Class<?> messageClass) {
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
    private void parseField(
            ByteBuf byteBuf, Object instance, ProtocolFieldMetadata fieldMetadata, ProtocolFrameMetadata msgMetadata)
            throws ProtocolException {

        // 使用字段处理器处理解析逻辑
        DataTypeConverter converter = getConverterFromCache(fieldMetadata.getDataType());
        protocolFieldProcessor.parseField(byteBuf, instance, fieldMetadata, msgMetadata, converter);
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
            Object instance, ByteBuf byteBuf, ProtocolFieldMetadata fieldMetadata, ProtocolFrameMetadata msgMetadata)
            throws ProtocolException {

        // 使用字段处理器处理序列化逻辑
        DataTypeConverter converter = getConverterFromCache(fieldMetadata.getDataType());
        protocolFieldProcessor.serializeField(instance, byteBuf, fieldMetadata, msgMetadata, converter);
    }

    /**
     * 构建消息元数据
     *
     * @param messageClass 消息类
     * @return 消息元数据
     */
    private ProtocolFrameMetadata buildMetadata(Class<?> messageClass) {
        ProtocolFrame protocolMessage = messageClass.getAnnotation(ProtocolFrame.class);
        if (protocolMessage == null) {
            throw new IllegalArgumentException("类必须标注 @ProtocolMessage 注解: " + messageClass.getName());
        }

        List<ProtocolFieldMetadata> fields = new ArrayList<>();

        // 收集所有标注了 @ProtocolField 的字段
        for (Field field : getAllFields(messageClass)) {
            ProtocolField protocolField = field.getAnnotation(ProtocolField.class);
            if (protocolField != null) {
                ProtocolValidation validation = field.getAnnotation(ProtocolValidation.class);
                fields.add(new ProtocolFieldMetadata(field, protocolField, validation));
            }
        }

        // 按 order 排序
        fields.sort(Comparator.comparingInt(ProtocolFieldMetadata::getOrder));

        return ProtocolFrameMetadata.create(messageClass, protocolMessage, fields);
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
     * 清理缓存
     */
    public void clearCache() {
        fieldCache.clear();
        converterCache.clear();
    }


    /**
     *  打印日志
     * @param title 标题
     * @param frameMetadata 元数据
     */
    private void printLog(String title, ProtocolFrameMetadata frameMetadata) {
        if (!log.isDebugEnabled()) {
            log.info(
                    """
                            
                            ┏━━━━━━━━━━━━━━━━━━━━━ {} ━━━━━━━━━━━━━━━━━━━━━┓
                            ┃ 消息类型: {}
                            ┃ 消息名称: {}
                            ┃ 消息描述: {}
                            ┃ 消息总长度: {} 字节
                            ┃ 字段数量: {}
                            ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                            """,
                    title,
                    frameMetadata.getFrameType(),
                    frameMetadata.getMessageName(),
                    frameMetadata.getDescription(),
                    frameMetadata.totalLength(),
                    frameMetadata.fields().size());
        } else {
            log.info(
                    "{} => 类型: {} | 名称: {} | 描述: {} | 长度: {}B | 字段数: {}",
                    title,
                    frameMetadata.getFrameType(),
                    frameMetadata.getMessageName(),
                    frameMetadata.getDescription(),
                    frameMetadata.totalLength(),
                    frameMetadata.fields().size());
        }
    }
}
