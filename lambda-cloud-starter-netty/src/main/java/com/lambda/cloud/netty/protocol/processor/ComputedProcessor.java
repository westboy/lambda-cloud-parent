package com.lambda.cloud.netty.protocol.processor;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ReflectUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.pool.ByteBufPool;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.ProtocolPayloadMetadata;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessorFactory;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolValidation;
import com.lambda.cloud.netty.protocol.checksum.ChecksumFactory;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterResolver;
import com.lambda.cloud.netty.protocol.model.SerializedData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * CRC处理器
 * <p>
 * 负责处理协议消息中的CRC校验计算和验证
 * </p>
 *
 * @author Jin
 */
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP")
public record ComputedProcessor(DataTypeConverterResolver converterResolver) {

    /**
     * 在序列化时计算并设置CRC值
     * <p>
     * 对所有标记为checksum=true的字段的原始hex报文数据进行CRC计算，
     * 然后将计算结果设置到所有CRCFiled=true的字段中
     * </p>
     *
     * @param message       消息实例
     * @param frameMetadata 消息元数据
     * @throws ProtocolException CRC处理异常
     */
    public void calculateAndSetCrc(Object message, ProtocolPayloadMetadata frameMetadata) throws ProtocolException {

        // 获取所有CRC字段（存储CRC值的字段）
        List<ProtocolFieldMetadata> crcAndLengthFields = getCrcAndLengthFields(frameMetadata);

        if (crcAndLengthFields.isEmpty()) {
            log.debug("消息中没有CRC字段，跳过CRC计算");
            return;
        }

        try {
            // 计算所有checksum=true字段的CRC值（只计算一次）
            SerializedData serializedData = calculateCrcAndDataLengthForAllComputedFields(message, frameMetadata);

            // 将计算出的 CRC 值设置到所有 CRC 字段中
            for (ProtocolFieldMetadata crcField : crcAndLengthFields) {
                if (crcField.isCrcField()) {
                    setValueToInstance(message, crcField, serializedData.getCrc());
                    log.debug("设置CRC字段: {} = {}", crcField.getFieldName(), serializedData.getCrc());
                }
                if (crcField.isLengthFiled()) {
                    setValueToInstance(message, crcField, serializedData.getLength());
                    log.debug("设置Length字段: {} = {}", crcField.getFieldName(), serializedData.getLength());
                }
            }

            log.debug("CRC计算和设置完成，共设置 {} 个CRC字段", crcAndLengthFields.size());

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.CRC_ERROR, "计算和设置CRC失败: " + e.getMessage(), "CRC处理", e);
        }
    }

    /**
     * 在解析时验证CRC值
     * <p>
     * 对所有标记为checksum=true的字段的原始hex报文数据进行CRC计算，
     * 然后与CRCFiled=true字段中存储的值进行比较
     * </p>
     *
     * @param instance      Object
     * @param raw           原始数据（ByteBuf）
     * @param frameMetadata 消息元数据
     * @throws ProtocolException CRC 验证失败
     */
    public void validateCrc(Object instance, ByteBuf raw, ProtocolPayloadMetadata frameMetadata)
            throws ProtocolException {

        // 获取所有 CRC 字段（存储CRC值的字段）
        List<ProtocolFieldMetadata> computedFields = getCrcAndLengthFields(frameMetadata);

        if (computedFields.isEmpty()) {
            log.debug("消息中没有CRC字段，跳过CRC验证");
            return;
        }

        // 计算所有checksum=true字段的CRC值（只计算一次）
        long calculatedCrc = calculateCrcByParsedDataList(raw, frameMetadata);

        // 验证每个 CRC 字段
        for (ProtocolFieldMetadata crcField : computedFields) {
            try {
                if (crcField.isLengthFiled()) {
                    continue;
                }
                // 获取实例中存储的 CRC 值
                long expectedCrc = getCrcValueFromInstance(instance, crcField);

                // 验证 CRC 值
                if (expectedCrc != calculatedCrc) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.CRC_VALIDATION_ERROR,
                            String.format(
                                    "原始报文%s CRC校验失败: %s, 期望值=0x%04X, 实际值=0x%04X",
                                    ByteBufUtil.hexDump(raw), crcField.getFieldName(), expectedCrc, calculatedCrc),
                            crcField.getFieldName());
                }

                log.debug("CRC校验通过: {} = {}", crcField.getFieldName(), expectedCrc);

            } catch (ProtocolException e) {
                throw e;
            } catch (Exception e) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.CRC_ERROR,
                        "验证CRC失败: " + crcField.getFieldName() + ", 原因: " + e.getMessage(),
                        crcField.getFieldName(),
                        e);
            }
        }
    }

    private long calculateCrcByParsedDataList(ByteBuf dataForCrc, ProtocolPayloadMetadata frameMetadata) {
        try {
            // 使用 CRC 算法计算校验值
            // 注意：ByteBuf 需要转为 byte[] 才能被 ChecksumFactory 使用，或者 ChecksumFactory 需要支持 ByteBuf
            // 考虑到 ChecksumFactory 接口可能只支持 byte[]，这里可能还是需要一次拷贝
            // 但如果在上层传入的是 slice，那么这里的拷贝也是基于 slice 的，比原来的 ByteArrayOutputStream 重建要好
            // 如果 ChecksumFactory 能升级支持 ByteBuf 会更好
            byte[] bytes = ByteBufUtil.getBytes(dataForCrc);
            long crcValue = ChecksumFactory.getAlgorithm(frameMetadata.getCrcAlgorithmName())
                    .calculate(bytes);

            log.debug("CRC计算完成，数据长度: {} bytes, CRC值: {}", dataForCrc.readableBytes(), crcValue);
            return crcValue;

        } catch (Exception e) {
            throw new RuntimeException("计算CRC失败", e);
        }
    }

    /**
     * 获取消息中的所有CRC喝LengthField字段（存储CRC值的字段）
     *
     * @param frameMetadata 消息元数据
     * @return CRC字段列表
     */
    private List<ProtocolFieldMetadata> getCrcAndLengthFields(ProtocolPayloadMetadata frameMetadata) {
        return frameMetadata.fields().stream()
                .filter(protocolFieldMetadata ->
                        protocolFieldMetadata.isCrcField() || protocolFieldMetadata.isLengthFiled())
                .collect(Collectors.toList());
    }

    /**
     * 获取消息中参与CRC计算的字段
     *
     * @param frameMetadata 消息元数据
     * @return 参与CRC计算的字段列表
     */
    private List<ProtocolFieldMetadata> getComputedFields(ProtocolPayloadMetadata frameMetadata) {
        return frameMetadata.fields().stream()
                .filter(ProtocolFieldMetadata::isComputed)
                .collect(Collectors.toList());
    }

    /**
     * 计算所有checksum=true字段的CRC值
     * <p>
     * 将所有标记为checksum=true的字段的原始hex报文数据按顺序拼接，
     * 然后对拼接后的完整数据进行一次CRC计算
     * </p>
     *
     * @param message       消息实例
     * @param frameMetadata 消息元数据
     * @return CRC值
     */
    private SerializedData calculateCrcAndDataLengthForAllComputedFields(
            Object message, ProtocolPayloadMetadata frameMetadata) {
        // 序列化消息到字节数组（用于CRC计算）
        SerializedData serializedData = new SerializedData();
        ByteBuf byteBuf = ByteBufPool.buffer();
        try {
            // 获取参与CRC计算的字段
            List<ProtocolFieldMetadata> computedFields = getComputedFields(frameMetadata);

            log.debug("开始计算CRC，参与计算的字段数: {}", computedFields.size());

            // 序列化参与CRC计算的字段，按字段顺序拼接原始hex报文数据
            for (ProtocolFieldMetadata fieldMetadata : computedFields) {
                // 获取字段值
                Object fieldValue = fieldMetadata.getValue(message);

                // 检查是否为复合字段
                if (fieldMetadata.isComposite()) {
                    this.serializeCompositeFieldForCrc(byteBuf, fieldValue, fieldMetadata);
                } else {
                    // 普通字段直接序列化
                    this.serializeFieldValue(byteBuf, fieldValue, fieldMetadata);
                }
                log.debug("字段 {} 参与CRC计算，值: {}", fieldMetadata.getFieldName(), fieldValue);
            }

            serializedData.setLength(byteBuf.readableBytes());
            byte[] dataForCrc = ByteBufUtil.getBytes(byteBuf);

            // 使用CRC算法计算校验值
            long crcValue = ChecksumFactory.getAlgorithm(frameMetadata.getCrcAlgorithmName())
                    .calculate(dataForCrc);
            if (log.isDebugEnabled()) {
                String raw = HexUtil.encodeHexStr(dataForCrc);
                log.debug("CRC计算完成，raw：{} 数据长度: {} bytes, CRC值: {}", raw, dataForCrc.length, crcValue);
            }
            serializedData.setCrc(crcValue);
            return serializedData;

        } catch (Exception e) {
            throw new RuntimeException("计算CRC失败", e);
        } finally {
            ByteBufPool.safeRelease(byteBuf);
        }
    }

    /**
     * 从实例中获取CRC值
     *
     * @param instance 消息实例
     * @param crcField CRC字段元数据
     * @return CRC值
     * @throws ProtocolException 获取失败
     */
    private long getCrcValueFromInstance(Object instance, ProtocolFieldMetadata crcField) throws ProtocolException {

        Object value = crcField.getValue(instance);

        switch (value) {
            case null -> {
                return 0L;
            }
            case Number number -> {
                return number.longValue();
            }
            case String hexString -> {
                // 移除可能的0x前缀
                if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
                    hexString = hexString.substring(2);
                }
                return Long.parseLong(hexString, 16);
            }
            default ->
                throw new ProtocolException(
                        ProtocolException.ErrorCode.CRC_ERROR,
                        "不支持的CRC值类型: " + value.getClass().getSimpleName(),
                        crcField.getFieldName());
        }
    }

    /**
     * 设置CRC值到实例
     *
     * @param instance      消息实例
     * @param fieldMetadata CRC字段元数据
     * @param crcValue      CRC值
     * @throws ProtocolException 设置失败
     */
    private void setValueToInstance(Object instance, ProtocolFieldMetadata fieldMetadata, long crcValue)
            throws ProtocolException {

        Class<?> fieldType = fieldMetadata.getFieldType();
        Object value;

        if (fieldType == String.class) {
            // 转换为十六进制字符串
            value = String.format("%0" + (fieldMetadata.getLength() * 2) + "X", crcValue);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            value = (int) crcValue;
        } else if (fieldType == Long.class || fieldType == long.class) {
            value = crcValue;
        } else {
            // 默认转换为十六进制字符串
            value = String.format("%0" + (fieldMetadata.getLength() * 2) + "X", crcValue);
        }
        fieldMetadata.setValue(instance, value);
    }

    /**
     * 递归序列化复合字段中参与CRC计算的子字段
     * <p>
     * 对于复合字段，需要递归处理其内部所有标记为checksum=true的子字段，
     * 而不是简单地序列化整个复合对象
     * </p>
     *
     * @param tempBuf        临时缓冲区
     * @param compositeValue 复合字段值
     * @param fieldMetadata  复合字段元数据
     * @throws ProtocolException 处理异常
     */
    private void serializeCompositeFieldForCrc(
            ByteBuf tempBuf, Object compositeValue, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (compositeValue == null) {
            log.debug("复合字段 {} 值为null，跳过CRC计算", fieldMetadata.getFieldName());
            return;
        }

        try {
            // 获取复合字段的类型
            Class<?> compositeType = compositeValue.getClass();

            // 获取复合字段内部的所有字段
            Field[] fields = compositeType.getDeclaredFields();

            log.debug("开始处理复合字段 {} 的子字段，共 {} 个字段", fieldMetadata.getFieldName(), fields.length);

            for (Field field : fields) {
                ProtocolField protocolField = field.getAnnotation(ProtocolField.class);

                // 只处理有ProtocolField注解且checksum=true的字段
                if (protocolField != null && protocolField.computed()) {
                    Object subFieldValue = ReflectUtil.getFieldValue(compositeValue, field);

                    // 创建子字段的元数据
                    ProtocolValidation subValidation = field.getAnnotation(ProtocolValidation.class);
                    FieldAccessor fieldAccessor = FieldAccessorFactory.createAccessor(field);
                    ProtocolFieldMetadata subFieldMetadata = new ProtocolFieldMetadata(
                            fieldAccessor, protocolField, subValidation, new ConcurrentHashMap<>(8));

                    // 递归处理子字段
                    if (subFieldMetadata.isComposite()) {
                        // 如果子字段也是复合字段，继续递归
                        this.serializeCompositeFieldForCrc(tempBuf, subFieldValue, subFieldMetadata);
                    } else {
                        // 普通子字段直接序列化
                        this.serializeFieldValue(tempBuf, subFieldValue, subFieldMetadata);
                    }

                    log.debug(
                            "复合字段 {} 的子字段 {} 参与CRC计算，值: {}",
                            fieldMetadata.getFieldName(),
                            field.getName(),
                            subFieldValue);
                }
            }

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "复合字段CRC计算失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 序列化字段值到ByteBuf
     *
     * @param byteBuf       字节缓冲区
     * @param fieldValue    字段值
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 序列化失败
     */
    public void serializeFieldValue(ByteBuf byteBuf, Object fieldValue, ProtocolFieldMetadata fieldMetadata)
            throws ProtocolException {
        try {
            DataTypeConverter converter = converterResolver.getConverter(fieldMetadata.getDataType());
            converter.serialize(fieldValue, byteBuf, fieldMetadata);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化字段失败: " + fieldMetadata.getFieldName(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    /**
     * 重置所有CRC和Length字段为0（用于序列化前占位）
     *
     * @param message       消息实例
     * @param frameMetadata 消息元数据
     * @throws ProtocolException 重置失败
     */
    public void resetCrcAndLengthFields(Object message, ProtocolPayloadMetadata frameMetadata)
            throws ProtocolException {
        List<ProtocolFieldMetadata> crcAndLengthFields = getCrcAndLengthFields(frameMetadata);
        for (ProtocolFieldMetadata field : crcAndLengthFields) {
            setValueToInstance(message, field, 0);
        }
    }

    /**
     * 在序列化后填充CRC和长度字段
     *
     * @param buffer          完整消息缓冲区
     * @param computedRanges  参与CRC计算的数据范围列表 [start, length]
     * @param crcFieldOffsets CRC/Length字段在缓冲区中的偏移量映射
     * @param frameMetadata   消息元数据
     * @param instance        消息实例（用于同步更新字段值，可选）
     * @throws ProtocolException 填充失败
     */
    public void fillCrcAndLength(
            ByteBuf buffer,
            List<int[]> computedRanges,
            Map<ProtocolFieldMetadata, Integer> crcFieldOffsets,
            ProtocolPayloadMetadata frameMetadata,
            Object instance)
            throws ProtocolException {

        if (crcFieldOffsets.isEmpty()) {
            return;
        }

        try {
            // 1. 计算CRC和总长度
            CompositeByteBuf dataForCrc = Unpooled.compositeBuffer();
            long totalLength = 0;
            for (int[] range : computedRanges) {
                int start = range[0];
                int length = range[1];
                if (length > 0) {
                    // slice() 返回视图，不发生内存拷贝
                    // retain() 确保引用计数，尽管在这里是在同一个线程栈中使用，Unpooled.compositeBuffer 不会释放底层 slice 除非 release
                    dataForCrc.addComponent(true, buffer.slice(start, length).retain());
                    totalLength += length;
                }
            }

            long crcValue = 0;
            try {
                if (dataForCrc.readableBytes() > 0) {
                    crcValue = calculateCrcByParsedDataList(dataForCrc, frameMetadata);
                }
            } finally {
                // 释放 CompositeByteBuf 及其持有的 slices 引用
                dataForCrc.release();
            }

            // 2. 填充 CRC 和 Length 字段
            for (Map.Entry<ProtocolFieldMetadata, Integer> entry : crcFieldOffsets.entrySet()) {
                ProtocolFieldMetadata field = entry.getKey();
                int offset = entry.getValue();

                if (field.isCrcField()) {
                    setBufferValue(buffer, offset, field, crcValue);
                    if (instance != null) {
                        setValueToInstance(instance, field, crcValue);
                        log.debug("序列化后更新CRC字段: {} = {}", field.getFieldName(), crcValue);
                    }
                }
                if (field.isLengthFiled()) {
                    setBufferValue(buffer, offset, field, totalLength);
                    if (instance != null) {
                        setValueToInstance(instance, field, totalLength);
                        log.debug("序列化后更新Length字段: {} = {}", field.getFieldName(), totalLength);
                    }
                }
            }

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.CRC_ERROR, "填充CRC/Length字段失败: " + e.getMessage(), "CRC处理", e);
        }
    }

    /**
     * 直接在ByteBuf的指定位置设置字段值
     *
     * @param buffer 缓冲区
     * @param offset 写入偏移量
     * @param field  字段元数据
     * @param value  数值（long）
     */
    private void setBufferValue(ByteBuf buffer, int offset, ProtocolFieldMetadata field, long value)
            throws ProtocolException {
        Object valObj = convertLongToFieldType(value, field);
        int oldWriterIndex = buffer.writerIndex();
        buffer.writerIndex(offset);
        try {
            serializeFieldValue(buffer, valObj, field);
        } finally {
            buffer.writerIndex(oldWriterIndex);
        }
    }

    /**
     * 将long值转换为字段对应的类型
     */
    private Object convertLongToFieldType(long value, ProtocolFieldMetadata field) {
        Class<?> type = field.getFieldType();
        if (type == String.class) {
            return String.format("%0" + (field.getLength() * 2) + "X", value);
        } else if (type == Integer.class || type == int.class) {
            return (int) value;
        } else if (type == Short.class || type == short.class) {
            return (short) value;
        } else if (type == Byte.class || type == byte.class) {
            return (byte) value;
        }
        return value; // Long
    }
}
