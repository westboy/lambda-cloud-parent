package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFieldProxy;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterResolver;
import com.lambda.cloud.netty.utils.ValidationUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * List转换器
 * <p>
 * 处理List集合字段的解析和序列化
 * 支持普通字段和复合字段的List集合
 * </p>
 *
 * @author zx
 */
@Slf4j
public record ListConverter(DataTypeConverterResolver converterResolver) implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (!fieldMetadata.isList()) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段不是List类型: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName());
        }

        log.debug("开始解析List字段: {}, 数据长度: {}", fieldMetadata.getFieldName(), data.length);

        try {
            DataTypeConverter elementConverter = getElementConverter(fieldMetadata);

            List<Object> result = new ArrayList<>(fieldMetadata.getListElementSize());

            // 如果是复合字段
            if (fieldMetadata.isComposite()) {
                ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);
                Object element = elementConverter.parse(data, elementMetadata);
                result.add(element);
            } else {
                int listSize = determineListSize(data, fieldMetadata);
                log.debug("List字段 {} 元素数量: {}", fieldMetadata.getFieldName(), listSize);
                int elementLength = calculateElementLength(data, fieldMetadata, listSize);
                log.debug("List字段 {} 元素长度: {}", fieldMetadata.getFieldName(), elementLength);
                int offset = 0;
                for (int i = 0; i < listSize; i++) {
                    if (offset + elementLength > data.length) {
                        throw new ProtocolException(
                                ProtocolException.ErrorCode.PARSE_ERROR,
                                String.format(
                                        "List元素 %d 数据不足，需要 %d 字节，剩余 %d 字节", i, elementLength, data.length - offset),
                                fieldMetadata.getFieldName());
                    }
                    // 提取元素数据
                    byte[] elementData = new byte[elementLength];
                    System.arraycopy(data, offset, elementData, 0, elementLength);
                    // 创建元素元数据
                    ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);
                    // 解析元素
                    Object element = elementConverter.parse(elementData, elementMetadata);
                    result.add(element);
                    offset += elementLength;
                    log.trace("解析List元素 {}: {}", i, element);
                }
                log.debug("List字段 {} 解析完成，共 {} 个元素", fieldMetadata.getFieldName(), result.size());
            }
            return result;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析List字段失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            return new byte[0];
        }

        if (!(value instanceof List<?> list)) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "值不是List类型: " + value.getClass().getSimpleName(),
                    fieldMetadata.getFieldName());
        }

        log.debug("开始序列化List字段: {}, 元素数量: {}", fieldMetadata.getFieldName(), list.size());

        try {
            // 1. 获取元素转换器
            DataTypeConverter elementConverter = getElementConverter(fieldMetadata);

            // 2. 创建元素元数据
            ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);

            // 3. 序列化List元素
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int elementIndex = 0;

            for (Object element : list) {
                byte[] elementBytes = elementConverter.serialize(element, elementMetadata);
                output.write(elementBytes);
                log.trace("序列化List元素 {}: {} -> {} 字节", elementIndex++, element, elementBytes.length);
            }

            byte[] result = output.toByteArray();
            log.debug("List字段 {} 序列化完成，总长度: {} 字节", fieldMetadata.getFieldName(), result.length);
            return result;

        } catch (IOException e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化List字段失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 简单实现：假设字符串格式为 "element1,element2,element3"
        String[] elements = value.split(",");
        List<Object> result = new ArrayList<>(elements.length);

        DataTypeConverter elementConverter = getElementConverter(fieldMetadata);
        ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);

        for (String element : elements) {
            Object parsedElement = elementConverter.parseFromString(element.trim(), elementMetadata);
            result.add(parsedElement);
        }

        return result;
    }

    /**
     * 确定 List 长度
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     * @return List长度
     */
    private int determineListSize(byte[] data, ProtocolFieldMetadata fieldMetadata) {
        if (fieldMetadata.isComposite()) {
            return data.length;
        }
        return data.length / fieldMetadata.getLength();
    }

    /**
     * 计算元素长度
     *
     * @param data          字节数据
     * @param fieldMetadata 字段元数据
     * @param listSize      List大小
     * @return 元素长度
     */
    private int calculateElementLength(byte[] data, ProtocolFieldMetadata fieldMetadata, int listSize)
            throws ProtocolException {

        // 根据数据类型获取默认长度
        int defaultLength = ValidationUtils.getDefaultElementLength(fieldMetadata.getListElementDataType());
        if (defaultLength > 0) {
            return defaultLength;
        }

        // 根据总数据长度和元素数量计算
        if (listSize > 0) {
            if (fieldMetadata.isComposite()) {
                return data.length;
            }
            return data.length / listSize;
        }

        throw new ProtocolException(
                ProtocolException.ErrorCode.PARSE_ERROR, "无法确定 List 元素长度", fieldMetadata.getFieldName());
    }

    /**
     * 获取元素转换器
     *
     * @param fieldMetadata 字段元数据
     * @return 元素转换器
     */
    private DataTypeConverter getElementConverter(ProtocolFieldMetadata fieldMetadata) {
        ProtocolDataType elementType = fieldMetadata.getListElementDataType();

        // 如果是复合类型，使用CompositeConverter
        if (fieldMetadata.isComposite()) {
            return converterResolver.getConverter(ProtocolDataType.COMPOSITE);
        }

        return converterResolver.getConverter(elementType);
    }

    /**
     * 创建元素元数据
     *
     * @param listMetadata List字段元数据
     * @return 元素元数据
     */
    private ProtocolFieldMetadata createElementMetadata(ProtocolFieldMetadata listMetadata) {
        return new ProtocolFieldMetadata(
                listMetadata.fieldAccessor(), new ProtocolFieldProxy(listMetadata), listMetadata.validation(),new ConcurrentHashMap<>(8));
    }
}
