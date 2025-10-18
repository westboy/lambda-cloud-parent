package com.lambda.cloud.netty.protocol.processor;

import com.lambda.cloud.netty.protocol.checksum.CrcChecksumService;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFrameMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * CRC处理器
 * <p>
 * 负责处理协议消息中的CRC校验计算和验证
 * </p>
 *
 * @param crcService CRC校验服务
 *                   -- GETTER --
 *                   获取CRC校验服务
 * @author Jin
 */
@Slf4j
public record CrcProcessor(CrcChecksumService crcService) {

    /**
     * 构造函数
     */
    public CrcProcessor() {
        this(new CrcChecksumService());
    }

    /**
     * 构造函数
     *
     * @param crcService CRC校验服务
     */
    public CrcProcessor {}

    /**
     * 在序列化时计算并设置CRC值
     *
     * @param message      消息实例
     * @param frameMetadata 消息元数据
     * @throws ProtocolException CRC处理异常
     */
    public void calculateAndSetCrc(Object message, ProtocolFrameMetadata frameMetadata) throws ProtocolException {

        // 获取所有CRC字段
        List<ProtocolFieldMetadata> crcFields = getCrcFields(frameMetadata);

        if (crcFields.isEmpty()) {
            log.debug("消息中没有CRC字段，跳过CRC计算");
            return;
        }

        for (ProtocolFieldMetadata crcField : crcFields) {
            try {
                // 计算CRC值（排除当前CRC字段）
                long crcValue = calculateCrcForMessage(message, frameMetadata, crcField);

                // 设置CRC值到实例
                setCrcValueToInstance(message, crcField, crcValue);

                log.debug("计算并设置CRC字段: {} = 0x{:04X}", crcField.getFieldName(), crcValue);

            } catch (Exception e) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.CRC_ERROR,
                        "计算CRC失败: " + crcField.getFieldName() + ", 原因: " + e.getMessage(),
                        crcField.getFieldName(),
                        e);
            }
        }
    }

    /**
     * 在解析时验证CRC值
     *
     * @param instance      消息实例
     * @param frameMetadata 消息元数据
     * @throws ProtocolException CRC验证失败
     */
    public void validateCrc(Object instance, ProtocolFrameMetadata frameMetadata) throws ProtocolException {

        // 获取所有 CRC 字段
        List<ProtocolFieldMetadata> crcFields = getCrcFields(frameMetadata);

        if (crcFields.isEmpty()) {
            log.debug("消息中没有CRC字段，跳过CRC验证");
            return;
        }

        for (ProtocolFieldMetadata crcField : crcFields) {
            try {
                // 获取实例中的CRC值
                long expectedCrc = getCrcValueFromInstance(instance, crcField);

                // 计算实际的CRC值（排除当前CRC字段）
                long actualCrc = calculateCrcForMessage(instance, frameMetadata, crcField);

                // 验证CRC值
                if (expectedCrc != actualCrc) {
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.CRC_VALIDATION_ERROR,
                            String.format(
                                    "CRC校验失败: %s, 期望值=0x%04X, 实际值=0x%04X",
                                    crcField.getFieldName(), expectedCrc, actualCrc),
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

    /**
     * 获取消息中的所有CRC字段（存储CRC值的字段）
     *
     * @param frameMetadata 消息元数据
     * @return CRC字段列表
     */
    private List<ProtocolFieldMetadata> getCrcFields(ProtocolFrameMetadata frameMetadata) {
        return frameMetadata.fields().stream()
                .filter(ProtocolFieldMetadata::isCrcField)
                .collect(Collectors.toList());
    }

    /**
     * 获取消息中参与CRC计算的字段
     *
     * @param frameMetadata 消息元数据
     * @return 参与CRC计算的字段列表
     */
    private List<ProtocolFieldMetadata> getCrcChecksumFields(ProtocolFrameMetadata frameMetadata) {
        return frameMetadata.fields().stream()
                .filter(ProtocolFieldMetadata::isCrcChecksum)
                .collect(Collectors.toList());
    }

    /**
     * 计算消息的CRC值（只对标记为checksum=true的字段进行计算）
     *
     * @param message         消息实例
     * @param frameMetadata   消息元数据
     * @param excludeCrcField 要排除的CRC字段
     * @return CRC值
     */
    private long calculateCrcForMessage(
            Object message, ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata excludeCrcField) {

        try {
            // 序列化消息到字节数组（用于CRC计算）
            ByteBuf tempBuf = Unpooled.buffer();

            // 获取参与CRC计算的字段
            List<ProtocolFieldMetadata> checksumFields = getCrcChecksumFields(frameMetadata);

            // 序列化参与CRC计算的字段
            for (ProtocolFieldMetadata fieldMetadata : checksumFields) {
                // 获取字段值并序列化
                Object fieldValue = CrcProcessorHelper.getFieldValue(message, fieldMetadata);
                CrcProcessorHelper.serializeFieldValue(tempBuf, fieldValue, fieldMetadata);
            }

            // 将ByteBuf转换为字节数组
            byte[] dataForCrc = new byte[tempBuf.readableBytes()];
            tempBuf.readBytes(dataForCrc);
            tempBuf.release();

            // 计算CRC
            return crcService.calculateMessageCrc(dataForCrc, frameMetadata, excludeCrcField);

        } catch (Exception e) {
            throw new RuntimeException("计算CRC失败", e);
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

        Object value = CrcProcessorHelper.getFieldValue(instance, crcField);

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
     * @param instance 消息实例
     * @param crcField CRC字段元数据
     * @param crcValue CRC值
     * @throws ProtocolException 设置失败
     */
    private void setCrcValueToInstance(Object instance, ProtocolFieldMetadata crcField, long crcValue)
            throws ProtocolException {

        Class<?> fieldType = crcField.getFieldType();
        Object value;

        if (fieldType == String.class) {
            // 转换为十六进制字符串
            value = String.format("%0" + (crcField.getLength() * 2) + "X", crcValue);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            value = (int) crcValue;
        } else if (fieldType == Long.class || fieldType == long.class) {
            value = crcValue;
        } else {
            // 默认转换为十六进制字符串
            value = String.format("%0" + (crcField.getLength() * 2) + "X", crcValue);
        }

        CrcProcessorHelper.setFieldValue(instance, crcField, value);
    }

    /**
     * 更新ByteBuf中的CRC字段数据
     *
     * @param byteBuf       字节缓冲区
     * @param frameMetadata 消息元数据
     * @param crcField      CRC字段元数据
     * @param crcValue      CRC值
     */
    private void updateCrcInByteBuf(
            ByteBuf byteBuf, ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata crcField, long crcValue)
            throws ProtocolException {

        // 计算CRC字段在ByteBuf中的偏移量
        int offset = calculateFieldOffset(frameMetadata, crcField);

        // 确定CRC算法
        String algorithmName = determineCrcAlgorithm(crcField);

        // 将CRC值转换为字节数组
        byte[] crcBytes = crcService.crcToBytes(crcValue, crcField, algorithmName);

        // 更新ByteBuf中的数据
        byteBuf.setBytes(offset, crcBytes);
    }

    /**
     * 计算字段在消息中的偏移量
     *
     * @param frameMetadata 消息元数据
     * @param targetField   目标字段
     * @return 偏移量
     */
    private int calculateFieldOffset(ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata targetField) {
        int offset = 0;

        for (ProtocolFieldMetadata fieldMetadata : frameMetadata.fields()) {
            if (fieldMetadata.equals(targetField)) {
                break;
            }
            offset += fieldMetadata.getLength();
        }

        return offset;
    }

    /**
     * 确定CRC算法名称
     *
     * @param crcField CRC字段元数据
     * @return 算法名称
     */
    private String determineCrcAlgorithm(ProtocolFieldMetadata crcField) {
        return CrcProcessorHelper.determineCrcAlgorithm(crcField);
    }
}
