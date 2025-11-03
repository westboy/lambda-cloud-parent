package com.lambda.cloud.netty.protocol.engine.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.util.*;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.ProtocolPayloadMetadata;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessorFactory;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterResolver;
import com.lambda.cloud.netty.protocol.converter.impl.CompositeConverter;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.processor.ComputedProcessor;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationEngine;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.EncryptionUtils;
import io.netty.buffer.ByteBuf;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 反射协议引擎
 * <p>
 * 基于反射的高性能协议解析引擎，支持缓存和优化
 * </p>
 *
 * @author Jin
 */
@Data
@Slf4j
public class ReflectionProtocolEngine implements ProtocolEngine<Object> {

    /**
     * 元数据缓存
     */
    private Map<Class<?>, ProtocolPayloadMetadata> metadataCache;

    /**
     * 数据类型转换器工厂
     */
    private DataTypeConverterResolver converterResolver;

    /**
     * 验证引擎
     */
    private ValidationEngine validationEngine;

    /**
     * 字段缓存管理器
     */
    private LRUCache<String, List<Field>> fieldCache;

    /**
     * 转换器缓存管理器
     */
    private LRUCache<String, DataTypeConverter> converterCache;
    /**
     * 字段处理器
     */
    private ProtocolFieldProcessor protocolFieldProcessor;

    /**
     * CRC 处理器
     */
    private ComputedProcessor computedProcessor;

    public ReflectionProtocolEngine(EncryptionService encryptionService) {
        this.fieldCache = CacheUtil.newLRUCache(1000);
        this.converterCache = CacheUtil.newLRUCache(100);
        this.metadataCache = new ConcurrentHashMap<>();
        this.validationEngine = new ValidationEngine();
        this.protocolFieldProcessor = new ProtocolFieldProcessor(encryptionService);
        this.converterResolver = new DataTypeConverterResolver();
        this.converterResolver.setEncryptionService(encryptionService);
        this.converterResolver.registerConverter(ProtocolDataType.COMPOSITE, new CompositeConverter(this));
        this.computedProcessor = new ComputedProcessor(converterResolver);
    }

    @Override
    public Object parse(ByteBuf byteBuf, Class<?> messageClass) throws ProtocolException {
        ProtocolPayloadMetadata metadata = getMetadata(messageClass);
        long startTime = System.nanoTime();
        try {
            // 记录解析开始
            logProtocolOperation("帧解析", metadata);
            // 创建消息实例
            Object protocolMessage = messageClass.getDeclaredConstructor().newInstance();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512)) {
                // 解析各个字段
                for (ProtocolFieldMetadata fieldMetadata : metadata.fields()) {
                    parseField(byteBuf, protocolMessage, fieldMetadata, metadata, outputStream);
                }
                if (metadata.isFrame()) {
                    // 获取参与 CRC 计算的原始数据
                    byte[] bytes = outputStream.toByteArray();
                    // 记录解析的原始数据（调试级别）
                    if (log.isDebugEnabled()) {
                        log.debug("解析原始数据：{}", HexUtil.encodeHexStr(bytes));
                    }
                    // 验证 CRC 校验和
                    computedProcessor.validateCrc(protocolMessage, bytes, metadata);
                }
            }
            // 记录解析成功和性能指标
            if (log.isDebugEnabled()) {
                long duration = System.nanoTime() - startTime;
                log.debug(
                        "协议帧解析完成 - 类型: {}, 耗时: {}μs, 剩余字节: {}",
                        messageClass.getSimpleName(),
                        duration / 1000,
                        byteBuf.readableBytes());
            }
            return protocolMessage;
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
        ProtocolPayloadMetadata metadata = getMetadata(message.getClass());
        long startTime = System.nanoTime();
        int initialWriterIndex = byteBuf.writerIndex();

        try {
            // 记录序列化开始
            logProtocolOperation("序列化", metadata);

            // 先计算并设置CRC校验和（在序列化前）
            computedProcessor.calculateAndSetCrc(message, metadata);

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
            ProtocolPayloadMetadata metadata = getMetadata(message.getClass());
            return validationEngine.validate(message, metadata);
        } catch (Exception e) {
            log.error("验证消息时发生异常", e);
            return ValidationResult.failure("验证过程中发生异常: " + e.getMessage());
        }
    }

    @Override
    public int calculateLength(Class<?> messageClass) {
        ProtocolPayloadMetadata metadata = getMetadata(messageClass);
        return metadata.totalLength();
    }

    @Override
    public ProtocolPayloadMetadata getMetadata(Class<?> messageClass) {
        return metadataCache.computeIfAbsent(messageClass, this::buildMetadata);
    }

    /**
     * 解析字段
     *
     * @param byteBuf           字节缓冲区
     * @param instance          目标实例
     * @param fieldMetadata     字段元数据
     * @param msgMetadata       消息元数据
     * @param outputStream 原始数据
     * @throws ProtocolException 解析异常
     */
    private void parseField(
            ByteBuf byteBuf,
            Object instance,
            ProtocolFieldMetadata fieldMetadata,
            ProtocolPayloadMetadata msgMetadata,
            ByteArrayOutputStream outputStream)
            throws ProtocolException, IOException {

        // 计算是否启用加密
        boolean encryptionEnabled = EncryptionUtils.isEncryptionEnabled(instance, msgMetadata);
        // 获取合适的转换器（支持复合字段与加密控制）
        DataTypeConverter converter = getConverter(fieldMetadata, encryptionEnabled);
        // 此处设置复合转换器解析
        protocolFieldProcessor.parseField(
                byteBuf, instance, fieldMetadata, msgMetadata, converter, encryptionEnabled, outputStream);
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
            Object instance, ByteBuf byteBuf, ProtocolFieldMetadata fieldMetadata, ProtocolPayloadMetadata msgMetadata)
            throws ProtocolException {

        // 计算是否启用加密
        boolean encryptionEnabled = EncryptionUtils.isEncryptionEnabled(instance, msgMetadata);

        // 获取合适的转换器（支持复合字段与加密控制）
        DataTypeConverter converter = getConverter(fieldMetadata, encryptionEnabled);
        // 此处设置复合转换器序列化
        protocolFieldProcessor.serializeField(
                instance, byteBuf, fieldMetadata, msgMetadata, converter, encryptionEnabled);
    }

    /**
     * 构建消息元数据
     *
     * @param messageClass 消息类
     * @return 消息元数据
     */
    private ProtocolPayloadMetadata buildMetadata(Class<?> messageClass) {
        ProtocolPayload protocolMessage = messageClass.getAnnotation(ProtocolPayload.class);
        if (protocolMessage == null) {
            throw new IllegalArgumentException("类必须标注 @ProtocolPayload 注解: " + messageClass.getName());
        }

        List<ProtocolFieldMetadata> fields = new ArrayList<>();
        List<Field> allFields = getAllFields(messageClass);
        for (Field field : allFields) {
            ProtocolField protocolField = field.getAnnotation(ProtocolField.class);
            if (protocolField != null) {
                // 校验：同一字段不可同时标注加密控制与加密数据
                if (protocolField.encryptedKey() && protocolField.encryptedField()) {
                    throw new IllegalArgumentException("同一字段不可同时标注 encryptedKey 与 encryptedField: " + field.getName());
                }
                ProtocolValidation validation = field.getAnnotation(ProtocolValidation.class);
                FieldAccessor fieldAccessor = FieldAccessorFactory.createAccessor(field);
                if (isGenericField(field)) {
                    Class<?> typeArgument = ClassUtil.getTypeArgument(messageClass);
                    if (typeArgument != null) {
                        fieldAccessor.setFieldType(typeArgument);
                    } else {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType pt) {
                            Type actualType = pt.getActualTypeArguments()[0];
                            Class<?> aClass = TypeUtil.getClass(actualType);
                            fieldAccessor.setFieldType(aClass);
                        }
                    }
                }
                fields.add(new ProtocolFieldMetadata(fieldAccessor, protocolField, validation,new ConcurrentHashMap<>(8)));
            }
        }

        // 按 order 排序
        fields.sort(Comparator.comparingInt(ProtocolFieldMetadata::getOrder));

        return ProtocolPayloadMetadata.create(messageClass, protocolMessage, fields);
    }

    public static boolean isGenericField(Field field) {
        Type type = field.getGenericType();
        return (type instanceof ParameterizedType) || (type instanceof TypeVariable);
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
     * 获取转换器（支持加密控制）
     */
    private DataTypeConverter getConverter(ProtocolFieldMetadata fieldMetadata, boolean encryptionEnabled) {
        // List 字段优先处理
        if (fieldMetadata.isList()) {
            return getListConverter();
        }
        // 复合字段优先
        if (fieldMetadata.isComposite()) {
            return getCompositeConverter();
        }
        // 仅当字段标记为加密且加密控制开启时，使用加密转换器
        if (encryptionEnabled && fieldMetadata.isEncryptedField()) {
            return getEncryptedConverter(fieldMetadata);
        }
        // 否则使用基础转换器
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
            converter = converterResolver.getConverter(dataType);
            converterCache.put(dataType.name(), converter);
        }
        return converter;
    }

    /**
     * 获取List字段转换器
     *
     * @return List字段转换器
     */
    private DataTypeConverter getListConverter() {
        DataTypeConverter converter = converterCache.get(ProtocolDataType.LIST.name());
        if (converter == null) {
            converter = converterResolver.getConverter(ProtocolDataType.LIST);
            converterCache.put(ProtocolDataType.LIST.name(), converter);
        }
        return converter;
    }

    /**
     * 获取复合字段转换器
     *
     * @return 复合字段转换器
     */
    private DataTypeConverter getCompositeConverter() {
        DataTypeConverter converter = converterCache.get(ProtocolDataType.COMPOSITE.name());
        if (converter == null) {
            converter = converterResolver.getConverter(ProtocolDataType.COMPOSITE);
            converterCache.put(ProtocolDataType.COMPOSITE.name(), converter);
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
        return converterResolver.getConverter(fieldMetadata);
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
    private void logProtocolOperation(String operation, ProtocolPayloadMetadata frameMetadata) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "协议{} - 类型: {}, 名称: {}, 长度: {}B, 字段数: {}",
                    operation,
                    frameMetadata.getFrameType(),
                    frameMetadata.getMessageName(),
                    frameMetadata.totalLength(),
                    frameMetadata.fields().size());
        }
    }
}
