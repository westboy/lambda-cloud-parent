package com.lambda.cloud.netty.protocol.checksum;

/**
 * CRC校验算法接口
 * <p>
 * 定义CRC校验计算的统一标准，支持多种CRC算法实现
 * </p>
 *
 * @author Jin
 */
public interface CrcAlgorithm {

    /**
     * 计算CRC校验值
     *
     * @param data 待校验的字节数据
     * @return CRC校验值
     */
    long calculate(byte[] data);

    /**
     * 获取CRC算法名称
     *
     * @return 算法名称
     */
    String algorithmName();

    /**
     * 获取CRC校验值的字节长度
     *
     * @return 字节长度
     */
    int getChecksumLength();

    /**
     * 验证CRC校验值
     *
     * @param data 原始数据
     * @param expectedChecksum 期望的校验值
     * @return 校验是否通过
     */
    default boolean verify(byte[] data, long expectedChecksum) {
        return calculate(data) == expectedChecksum;
    }

    /**
     * 将CRC校验值转换为字节数组
     *
     * @param checksum CRC校验值
     * @param littleEndian 是否小端序
     * @return 字节数组
     */
    default byte[] toBytes(long checksum, boolean littleEndian) {
        int length = getChecksumLength();
        byte[] result = new byte[length];

        if (littleEndian) {
            // 小端序：低位字节在前
            for (int i = 0; i < length; i++) {
                result[i] = (byte) (checksum >> (i * 8));
            }
        } else {
            // 大端序：高位字节在前
            for (int i = 0; i < length; i++) {
                result[i] = (byte) (checksum >> ((length - 1 - i) * 8));
            }
        }

        return result;
    }

    /**
     * 从字节数组解析CRC校验值
     *
     * @param bytes 字节数组
     * @param littleEndian 是否小端序
     * @return CRC校验值
     */
    default long fromBytes(byte[] bytes, boolean littleEndian) {
        long result = 0;

        if (littleEndian) {
            // 小端序：低位字节在前
            for (int i = 0; i < bytes.length; i++) {
                result |= ((long) (bytes[i] & 0xFF)) << (i * 8);
            }
        } else {
            // 大端序：高位字节在前
            for (byte aByte : bytes) {
                result = (result << 8) | (aByte & 0xFF);
            }
        }

        return result;
    }
}
