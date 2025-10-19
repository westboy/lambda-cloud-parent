package com.lambda.cloud.netty.protocol.engine.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.converter.impl.CompositeConverter;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFrameMetadata;
import com.lambda.cloud.netty.protocol.processor.CrcProcessor;
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
    private final LRUCache<String, List<Field>> fieldCache = CacheUtil.newLRUCache(1000);

    /**
     * 转换器缓存管理器
     */
    private final LRUCache<String, DataTypeConverter> converterCache = CacheUtil.newLRUCache(100);
    /**
     * 字段处理器
     */
    private final ProtocolFieldProcessor protocolFieldProcessor = new ProtocolFieldProcessor();

    /**
     * CRC处理器
     */
    private final CrcProcessor crcProcessor = new CrcProcessor();

    @Override
    public Object parse(ByteBuf byteBuf, Class<Object> messageClass) throws ProtocolException {
        ProtocolFrameMetadata metadata = getMetadata(messageClass);
        long startTime = System.nanoTime();

        try {
            // 记录解析开始
            logProtocolOperation("帧解析", metadata);

            // 创建消息实例
            Object instance = messageClass.getDeclaredConstructor().newInstance();

            // 解析各个字段
            for (ProtocolFieldMetadata fieldMetadata : metadata.fields()) {
                parseField(byteBuf, instance, fieldMetadata, metadata);
            }

            // 验证CRC校验和
            crcProcessor.validateCrc(instance, metadata);

            // 记录解析成功和性能指标
            if (log.isDebugEnabled()) {
                long duration = System.nanoTime() - startTime;
                log.debug(
                        "协议帧解析完成 - 类型: {}, 耗时: {}μs, 剩余字节: {}",
                        messageClass.getSimpleName(),
                        duration / 1000,
                        byteBuf.readableBytes());
            }

            return instance;
        } catch (Exception e) {
            // 记录解析失败和性能指标
            long duration = System.nanoTime() - startTime;
            log.error(
                    "协议帧解析失败 - 类型: {}, 耗时: {}μs, 错误: {}",
                    messageClass.getSimpleName(),
                    duration / 1000,
                    e.getMessage());

            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "解析消息失败: " + messageClass.getSimpleName(), e);
        }
    }

    @Override
    public void serialize(Object message, ByteBuf byteBuf) throws ProtocolException {
        ProtocolFrameMetadata metadata = getMetadata(message.getClass());
        long startTime = System.nanoTime();
        int initialWriterIndex = byteBuf.writerIndex();

        try {
            // 记录序列化开始
            logProtocolOperation("序列化", metadata);

            // 先计算并设置CRC校验和（在序列化前）
            crcProcessor.calculateAndSetCrc(message, metadata);

            // 序列化各个字段
            for (ProtocolFieldMetadata fieldMetadata : metadata.fields()) {
                serializeField(message, byteBuf, fieldMetadata, metadata);
            }

            // 记录序列化成功和性能指标
            if (log.isDebugEnabled()) {
                long duration = System.nanoTime() - startTime;
                int bytesWritten = byteBuf.writerIndex() - initialWriterIndex;
                log.debug(
                        "协议帧序列化完成 - 类型: {}, 耗时: {}μs, 写入字节: {}, 缓冲区容量: {}",
                        message.getClass().getSimpleName(),
                        duration / 1000,
                        bytesWritten,
                        byteBuf.capacity());
            }

        } catch (Exception e) {
            // 记录序列化失败和性能指标
            long duration = System.nanoTime() - startTime;
            int bytesWritten = byteBuf.writerIndex() - initialWriterIndex;
            log.error(
                    "协议帧序列化失败 - 类型: {}, 耗时: {}μs, 已写入字节: {}, 错误: {}",
                    message.getClass().getSimpleName(),
                    duration / 1000,
                    bytesWritten,
                    e.getMessage());

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

        // 获取合适的转换器（支持复合字段）
        DataTypeConverter converter = getConverter(fieldMetadata);
        // 此处设置复合转换器解析
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

        // 获取合适的转换器（支持复合字段）
        DataTypeConverter converter = getConverter(fieldMetadata);
        // 此处设置复合转换器序列化
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
     * 获取转换器（支持复合字段）
     *
     * @param fieldMetadata 字段元数据
     * @return 转换器
     */
    private DataTypeConverter getConverter(ProtocolFieldMetadata fieldMetadata) {
        // 检查是否为复合字段
        if (fieldMetadata.isComposite()) {
            return getCompositeConverter();
        }

        // 检查是否为加密字段
        if (fieldMetadata.isEncrypted()) {
            return getEncryptedConverter(fieldMetadata);
        }

        // 普通字段使用数据类型转换器
        return getConverterFromCache(fieldMetadata.getDataType());
    }

    /**
     * 从缓存获取转换器
     *
     * @param dataType 数据类型
     * @return 转换器
     */
    private DataTypeConverter getConverterFromCache(ProtocolDataType dataType) {
        DataTypeConverter converter = converterCache.get(dataType.name());
        if (converter == null) {
            converter = converterFactory.getConverter(dataType);
            converterCache.put(dataType.name(), converter);
        }
        return converter;
    }

    /**
     * 获取复合字段转换器
     *
     * @return 复合字段转换器
     */
    private DataTypeConverter getCompositeConverter() {
        DataTypeConverter converter = converterCache.get("COMPOSITE_KEY");
        if (converter == null) {
            converter = new CompositeConverter(this);
            converterCache.put("COMPOSITE_KEY", converter);
        }
        return converter;
    }

    /**
     * 获取加密字段转换器
     *
     * @param fieldMetadata 字段元数据
     * @return 加密字段转换器
     */
    private DataTypeConverter getEncryptedConverter(ProtocolFieldMetadata fieldMetadata) {
        // 使用转换器工厂的加密支持
        return converterFactory.getConverter(fieldMetadata);
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        fieldCache.clear();
        converterCache.clear();
    }

    /**
     * 打印协议操作日志
     * <p>
     * 根据日志级别输出不同详细程度的信息：
     * - INFO级别：输出简洁的操作摘要
     * - DEBUG级别：输出详细的协议信息
     * </p>
     *
     * @param operation     操作类型（如"帧解析"、"序列化"）
     * @param frameMetadata 协议帧元数据
     */
    private void logProtocolOperation(String operation, ProtocolFrameMetadata frameMetadata) {
        if (log.isInfoEnabled()) {
            log.info(
                    "协议{} - 类型: {}, 名称: {}, 长度: {}B, 字段数: {}",
                    operation,
                    frameMetadata.getFrameType(),
                    frameMetadata.getMessageName(),
                    frameMetadata.totalLength(),
                    frameMetadata.fields().size());
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    """

                            ┏━━━━━━━━━━━━━━━━━━━━━ 协议{} ━━━━━━━━━━━━━━━━━━━━━┓
                            ┃ 消息类型: {}
                            ┃ 消息名称: {}
                            ┃ 消息描述: {}
                            ┃ 消息总长度: {} 字节
                            ┃ 字段数量: {}
                            ┃ 线程: {}
                            ┃ 时间戳: {}
                            ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                            """,
                    operation,
                    frameMetadata.getFrameType(),
                    frameMetadata.getMessageName(),
                    frameMetadata.getDescription(),
                    frameMetadata.totalLength(),
                    frameMetadata.fields().size(),
                    Thread.currentThread().getName(),
                    System.currentTimeMillis());
        }
    }
}
