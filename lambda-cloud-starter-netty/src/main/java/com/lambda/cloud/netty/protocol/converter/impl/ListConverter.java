package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.impl.VirtualFieldAccessor;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFieldProxy;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterResolver;
import com.lambda.cloud.netty.utils.ValidationUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
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
@SuppressFBWarnings("EI_EXPOSE_REP")
public record ListConverter(DataTypeConverterResolver converterResolver) implements DataTypeConverter {

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (!fieldMetadata.isList()) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段不是List类型: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName());
        }

        log.debug("开始解析List字段: {}, 数据长度: {}", fieldMetadata.getFieldName(), length);

        try {
            DataTypeConverter elementConverter = getElementConverter(fieldMetadata);

            List<Object> result = new ArrayList<>(fieldMetadata.getListElementSize());

            // 创建切片以限制读取范围
            ByteBuf slice = buffer.readSlice(length);

            // 如果是复合字段
            if (fieldMetadata.isComposite()) {
                int elementLength = fieldMetadata.getLength();
                Object ext = fieldMetadata.extParam().get("ListElementLength");
                if ((elementLength <= 0) && (ext instanceof Number number)) {
                    elementLength = number.intValue();
                }
                if (elementLength <= 0) {
                    int listSize = fieldMetadata.getListElementSize();
                    if (listSize > 0) {
                        elementLength = length / listSize;
                    }
                }
                if (elementLength <= 0) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.PARSE_ERROR,
                            "复合List元素长度必须大于0: " + fieldMetadata.getFieldName(),
                            fieldMetadata.getFieldName());
                }
                if (length % elementLength != 0) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.PARSE_ERROR,
                            "复合List长度无法整除元素长度: " + fieldMetadata.getFieldName(),
                            fieldMetadata.getFieldName());
                }
                int listSize = length / elementLength;
                ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);
                for (int i = 0; i < listSize; i++) {
                    ByteBuf elementSlice = slice.readSlice(elementLength);
                    Object element = elementConverter.parse(elementSlice, elementLength, elementMetadata);
                    result.add(element);
                }
            } else {
                int listSize = determineListSize(length, fieldMetadata);
                log.debug("List字段 {} 元素数量: {}", fieldMetadata.getFieldName(), listSize);
                int elementLength = calculateElementLength(length, fieldMetadata, listSize);
                log.debug("List字段 {} 元素长度: {}", fieldMetadata.getFieldName(), elementLength);
                int offset = 0;
                for (int i = 0; i < listSize; i++) {
                    if (offset + elementLength > length) {
                        throw new ProtocolException(
                                ProtocolException.ErrorCode.PARSE_ERROR,
                                String.format("List元素 %d 数据不足，需要 %d 字节，剩余 %d 字节", i, elementLength, length - offset),
                                fieldMetadata.getFieldName());
                    }

                    // 创建元素切片
                    ByteBuf elementSlice = slice.readSlice(elementLength);

                    // 创建元素元数据
                    ProtocolFieldMetadata elementMetadata = createElementMetadata(fieldMetadata);
                    // 解析元素
                    Object element = elementConverter.parse(elementSlice, elementLength, elementMetadata);
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
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            return;
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
            int elementIndex = 0;

            for (Object element : list) {
                int startWriterIndex = buffer.writerIndex();
                elementConverter.serialize(element, buffer, elementMetadata);
                int bytesWritten = buffer.writerIndex() - startWriterIndex;
                log.trace("序列化List元素 {}: {} -> {} 字节", elementIndex++, element, bytesWritten);
            }

            log.debug("List字段 {} 序列化完成", fieldMetadata.getFieldName());

        } catch (Exception e) {
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
     * @param totalLength   总数据长度
     * @param fieldMetadata 字段元数据
     * @return List长度
     */
    private int determineListSize(int totalLength, ProtocolFieldMetadata fieldMetadata) {
        if (fieldMetadata.isComposite()) {
            return totalLength;
        }
        return totalLength / fieldMetadata.getLength();
    }

    /**
     * 计算元素长度
     *
     * @param totalLength   总数据长度
     * @param fieldMetadata 字段元数据
     * @param listSize      List大小
     * @return 元素长度
     */
    private int calculateElementLength(int totalLength, ProtocolFieldMetadata fieldMetadata, int listSize)
            throws ProtocolException {

        // 根据数据类型获取默认长度
        int defaultLength = ValidationUtils.getDefaultElementLength(fieldMetadata.getListElementDataType());
        if (defaultLength > 0) {
            return defaultLength;
        }

        // 根据总数据长度和元素数量计算
        if (listSize > 0) {
            if (fieldMetadata.isComposite()) {
                return totalLength;
            }
            return totalLength / listSize;
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
        FieldAccessor fieldAccessor = listMetadata.fieldAccessor();
        if (listMetadata.isComposite()) {
            Class<?> elementClass = listMetadata.getListElementClass();
            if (elementClass != null) {
                fieldAccessor = new VirtualFieldAccessor(listMetadata.getFieldName() + "[]", elementClass);
            }
        }
        ProtocolFieldMetadata elementMetadata = new ProtocolFieldMetadata(
                fieldAccessor, new ProtocolFieldProxy(listMetadata), listMetadata.validation(), new ConcurrentHashMap<>(8));
        if (listMetadata.isComposite()) {
            Class<?> elementClass = listMetadata.getListElementClass();
            if (elementClass != null) {
                elementMetadata.extParam().put("CompositeType", elementClass);
            }
        }
        return elementMetadata;
    }
}
