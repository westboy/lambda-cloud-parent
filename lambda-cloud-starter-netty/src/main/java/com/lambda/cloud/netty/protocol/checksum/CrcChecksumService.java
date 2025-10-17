package com.lambda.cloud.netty.protocol.checksum;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
import com.lambda.cloud.netty.protocol.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFrameMetadata;
import com.lambda.cloud.netty.protocol.processor.CrcProcessorHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * CRC校验服务
 * <p>
 * 负责协议消息的CRC计算和验证，支持多种CRC算法
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class CrcChecksumService {

    /**
     * CRC算法缓存
     */
    private final Map<String, CrcAlgorithm> algorithmCache = new ConcurrentHashMap<>();

    /**
     * 默认构造函数
     */
    public CrcChecksumService() {
        // 注册默认的CRC算法
        registerAlgorithm("CRC16", new Crc16Algorithm());
        registerAlgorithm("CRC16-CCITT", Crc16Algorithm.ccitt());
        registerAlgorithm("CRC16-IBM", Crc16Algorithm.ibm());
        registerAlgorithm("CRC16-MAXIM", Crc16Algorithm.maxim());
        registerAlgorithm("CRC16-USB", Crc16Algorithm.usb());
        registerAlgorithm("CRC16-X25", Crc16Algorithm.x25());
        registerAlgorithm("CRC16-XMODEM", Crc16Algorithm.xmodem());
    }

    /**
     * 注册CRC算法
     *
     * @param name      算法名称
     * @param algorithm CRC算法实现
     */
    public void registerAlgorithm(String name, CrcAlgorithm algorithm) {
        algorithmCache.put(name, algorithm);
        log.debug("注册CRC算法: {}", name);
    }

    /**
     * 获取CRC算法
     *
     * @param algorithmName 算法名称
     * @return CRC算法实现
     * @throws ProtocolException 算法不存在时抛出异常
     */
    public CrcAlgorithm getAlgorithm(String algorithmName) throws ProtocolException {
        CrcAlgorithm algorithm = algorithmCache.get(algorithmName);
        if (algorithm == null) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.VALIDATION_ERROR, "不支持的CRC算法: " + algorithmName, "CRC算法");
        }
        return algorithm;
    }

    /**
     * 计算消息的CRC校验值
     *
     * @param messageData   完整消息数据
     * @param frameMetadata 消息元数据
     * @param crcField      CRC字段元数据
     * @return CRC校验值
     * @throws ProtocolException 计算异常
     */
    public long calculateMessageCrc(
            byte[] messageData, ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata crcField)
            throws ProtocolException {

        // 获取CRC算法（默认使用CRC16）
        String algorithmName = determineCrcAlgorithm(crcField);
        CrcAlgorithm algorithm = getAlgorithm(algorithmName);

        // 计算CRC校验范围的数据
        byte[] dataForCrc = extractDataForCrc(messageData, frameMetadata, crcField);

        // 计算CRC值
        long crcValue = algorithm.calculate(dataForCrc);

        log.debug("计算CRC校验值: 算法={}, 数据长度={}, CRC值=0x{:04X}", algorithmName, dataForCrc.length, crcValue);

        return crcValue;
    }

    /**
     * 验证消息的CRC校验值
     *
     * @param messageData   完整消息数据
     * @param frameMetadata 消息元数据
     * @param crcField      CRC字段元数据
     * @param expectedCrc   期望的CRC值
     * @return 校验是否通过
     * @throws ProtocolException 验证异常
     */
    public boolean verifyMessageCrc(
            byte[] messageData, ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata crcField, long expectedCrc)
            throws ProtocolException {

        long calculatedCrc = calculateMessageCrc(messageData, frameMetadata, crcField);
        boolean isValid = calculatedCrc == expectedCrc;

        if (!isValid) {
            log.warn("CRC校验失败: 期望=0x{:04X}, 实际=0x{:04X}, 字段={}", expectedCrc, calculatedCrc, crcField.getFieldName());
        } else {
            log.debug("CRC校验通过: 值=0x{:04X}, 字段={}", calculatedCrc, crcField.getFieldName());
        }

        return isValid;
    }

    /**
     * 确定CRC算法名称
     *
     * @param crcField CRC字段元数据
     * @return 算法名称
     */
    private String determineCrcAlgorithm(ProtocolFieldMetadata crcField) {
        // 根据字段长度确定默认算法
        return CrcProcessorHelper.determineCrcAlgorithm(crcField);
    }

    /**
     * 提取用于CRC计算的数据
     * <p>
     * CRC计算范围：除CRC字段外的所有字段数据
     * </p>
     *
     * @param messageData   完整消息数据
     * @param frameMetadata 消息元数据
     * @param crcField      CRC字段元数据
     * @return 用于CRC计算的数据
     */
    private byte[] extractDataForCrc(
            byte[] messageData, ProtocolFrameMetadata frameMetadata, ProtocolFieldMetadata crcField) {

        List<ProtocolFieldMetadata> fields = frameMetadata.fields();
        ByteBuf buffer = Unpooled.buffer();

        try {
            int currentOffset = 0;

            // 遍历所有字段，排除CRC字段
            for (ProtocolFieldMetadata field : fields) {
                if (field.equals(crcField)) {
                    // 跳过CRC字段
                    currentOffset += field.getLength();
                    continue;
                }

                // 添加非CRC字段的数据
                int fieldLength = field.getLength();
                if (currentOffset + fieldLength <= messageData.length) {
                    buffer.writeBytes(messageData, currentOffset, fieldLength);
                }
                currentOffset += fieldLength;
            }

            // 转换为字节数组
            byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            return result;

        } finally {
            buffer.release();
        }
    }

    /**
     * 将CRC值转换为字节数组
     *
     * @param crcValue     CRC值
     * @param crcField     CRC字段元数据
     * @param algorithmName 算法名称
     * @return 字节数组
     * @throws ProtocolException 转换异常
     */
    public byte[] crcToBytes(long crcValue, ProtocolFieldMetadata crcField, String algorithmName)
            throws ProtocolException {

        CrcAlgorithm algorithm = getAlgorithm(algorithmName);
        return algorithm.toBytes(crcValue, crcField.isLittleEndian());
    }

    /**
     * 从字节数组解析CRC值
     *
     * @param crcBytes     CRC字节数据
     * @param crcField     CRC字段元数据
     * @param algorithmName 算法名称
     * @return CRC值
     * @throws ProtocolException 解析异常
     */
    public long bytesToCrc(byte[] crcBytes, ProtocolFieldMetadata crcField, String algorithmName)
            throws ProtocolException {

        CrcAlgorithm algorithm = getAlgorithm(algorithmName);
        return algorithm.fromBytes(crcBytes, crcField.isLittleEndian());
    }
}
