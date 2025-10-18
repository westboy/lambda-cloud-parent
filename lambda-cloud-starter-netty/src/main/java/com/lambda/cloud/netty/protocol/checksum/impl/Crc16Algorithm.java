package com.lambda.cloud.netty.protocol.checksum.impl;

import cn.hutool.core.io.checksum.crc16.*;
import com.lambda.cloud.netty.protocol.checksum.CrcAlgorithm;

/**
 * CRC16校验算法实现
 * <p>
 * 基于Hutool的CRC16Checksum实现，支持标准CRC16算法
 * </p>
 *
 * @param crc16Checksum CRC16校验器实例
 * @param algorithmName 算法名称
 * @author Jin
 */
public record Crc16Algorithm(CRC16Checksum crc16Checksum, String algorithmName) implements CrcAlgorithm {

    /**
     * 默认构造函数，使用标准CRC16算法
     */
    public Crc16Algorithm() {
        this(ccitt().crc16Checksum(), "CRC16-CCITT");
    }

    /**
     * 构造函数
     *
     * @param crc16Checksum CRC16校验器
     * @param algorithmName 算法名称
     */
    public Crc16Algorithm {
    }

    @Override
    public long calculate(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }

        // 重置校验器状态
        crc16Checksum.reset();

        // 更新数据
        crc16Checksum.update(data, 0, data.length);

        // 获取校验值
        return crc16Checksum.getValue();
    }

    @Override
    public int getChecksumLength() {
        // CRC16校验值为2字节
        return 2;
    }

    /**
     * 创建CRC16-CCITT算法实例
     *
     * @return CRC16-CCITT算法实例
     */
    public static Crc16Algorithm ccitt() {
        return new Crc16Algorithm(new CRC16CCITT(), "CRC16-CCITT");
    }

    /**
     * 创建CRC16-IBM算法实例
     *
     * @return CRC16-IBM算法实例
     */
    public static Crc16Algorithm ibm() {
        return new Crc16Algorithm(new CRC16IBM(), "CRC16-IBM");
    }

    /**
     * 创建CRC16-MAXIM算法实例
     *
     * @return CRC16-MAXIM算法实例
     */
    public static Crc16Algorithm maxim() {
        return new Crc16Algorithm(new CRC16Maxim(), "CRC16-MAXIM");
    }

    /**
     * 创建CRC16-USB算法实例
     *
     * @return CRC16-USB算法实例
     */
    public static Crc16Algorithm usb() {
        return new Crc16Algorithm(new CRC16USB(), "CRC16-USB");
    }

    /**
     * 创建CRC16-X25算法实例
     *
     * @return CRC16-X25算法实例
     */
    public static Crc16Algorithm x25() {
        return new Crc16Algorithm(new CRC16X25(), "CRC16-X25");
    }

    /**
     * 创建CRC16-XMODEM算法实例
     *
     * @return CRC16-XMODEM算法实例
     */
    public static Crc16Algorithm xmodem() {
        return new Crc16Algorithm(new CRC16XModem(), "CRC16-XMODEM");
    }
}
