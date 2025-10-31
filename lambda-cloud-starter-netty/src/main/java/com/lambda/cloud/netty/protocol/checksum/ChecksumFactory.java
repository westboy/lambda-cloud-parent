package com.lambda.cloud.netty.protocol.checksum;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
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
public final class ChecksumFactory {

    /**
     * 获取CRC算法
     *
     * @param algorithmName 算法名称
     * @return CRC算法实现
     */
    public static CrcAlgorithm getAlgorithm(String algorithmName) {
        CrcAlgorithm algorithm = Crc16Algorithm.modbus();
        switch (algorithmName) {
            case "CRC16-CCITT" -> Crc16Algorithm.ccitt();
            case "CRC16-IBM" -> Crc16Algorithm.ibm();
            case "CRC16-MAXIM" -> Crc16Algorithm.maxim();
            case "CRC16-USB" -> Crc16Algorithm.usb();
            case "CRC16-X25" -> Crc16Algorithm.x25();
            case "CRC16-XMODEM" -> Crc16Algorithm.xmodem();
            case "CRC16-MODBUS" -> Crc16Algorithm.modbus();
        }
        return algorithm;
    }
}
