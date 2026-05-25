package com.lambda.cloud.netty.protocol.checksum.impl;

import com.lambda.cloud.netty.protocol.checksum.CrcAlgorithm;

/**
 * SUM8 累加和校验算法实现
 * <p>
 * 该算法用于 DL/T 645-2007 等工业协议的帧校验计算。
 * 计算逻辑：将数据域所有字节按无符号方式累加，取低 8 位作为校验值。
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>Java 中 {@code byte} 类型是有符号的（-128~127），因此在累加时需使用 {@code (b & 0xFF)} 将其转换为无符号整数，避免符号扩展导致的计算错误。</li>
 *   <li>最终结果使用 {@code sum & 0xFF} 截取低 8 位，确保返回值在 0~255 范围内。</li>
 * </ul>
 * </p>
 *
 * @author Jin
 * @see CrcAlgorithm
 */
public record Sum8Algorithm() implements CrcAlgorithm {

    /**
     * 计算 SUM8 校验值
     * <p>
     * 算法步骤：
     * 1. 遍历输入字节数组，将每个字节转换为无符号整数（0~255）后累加
     * 2. 取累加和的低 8 位作为最终校验值
     * </p>
     * <p>
     * 示例：对于字节数组 {@code [0x68, 0x11, 0x22]}，计算过程为：
     * {@code (0x68 & 0xFF) + (0x11 & 0xFF) + (0x22 & 0xFF) = 104 + 17 + 34 = 155 (0x9B)}
     * </p>
     *
     * @param data 待校验的字节数组
     * @return 校验值（0~255），若输入为空则返回 0
     */
    @Override
    public long calculate(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        int sum = 0;
        for (byte b : data) {
            // 使用 (b & 0xFF) 将有符号 byte 转换为无符号整数，避免符号扩展
            sum += (b & 0xFF);
        }
        // 取低 8 位，确保结果在 0~255 范围内
        return sum & 0xFF;
    }

    /**
     * 获取算法名称
     *
     * @return 算法标识名称 "SUM8"
     */
    @Override
    public String algorithmName() {
        return "SUM8";
    }

    /**
     * 获取校验值占用的字节长度
     *
     * @return 校验值长度，固定为 1 字节
     */
    @Override
    public int getChecksumLength() {
        return 1;
    }
}
