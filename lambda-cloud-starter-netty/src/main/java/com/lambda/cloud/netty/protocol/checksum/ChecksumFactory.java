package com.lambda.cloud.netty.protocol.checksum;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
import com.lambda.cloud.netty.protocol.checksum.impl.Sum8Algorithm;
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
public final class ChecksumFactory {

    private static final ConcurrentHashMap<String, CrcAlgorithm> ALGORITHM_REGISTRY = new ConcurrentHashMap<>();

    static {
        register("crc16-ccitt", Crc16Algorithm.ccitt());
        register("crc16-ibm", Crc16Algorithm.ibm());
        register("crc16-maxim", Crc16Algorithm.maxim());
        register("crc16-usb", Crc16Algorithm.usb());
        register("crc16-x25", Crc16Algorithm.x25());
        register("crc16-xmodem", Crc16Algorithm.xmodem());
        register("crc16-modbus", Crc16Algorithm.modbus());
        register("sum8", new Sum8Algorithm());
    }

    public static void register(String name, CrcAlgorithm algorithm) {
        ALGORITHM_REGISTRY.put(name.toLowerCase(), algorithm);
        log.debug("Registered checksum algorithm: {}", name);
    }

    public static CrcAlgorithm getAlgorithm(String algorithmName) {
        if (algorithmName == null) {
            return ALGORITHM_REGISTRY.get("crc16-modbus");
        }
        CrcAlgorithm algorithm = ALGORITHM_REGISTRY.get(algorithmName.toLowerCase());
        if (algorithm == null) {
            log.warn("Unknown checksum algorithm: {}, falling back to CRC16-MODBUS", algorithmName);
            return ALGORITHM_REGISTRY.get("crc16-modbus");
        }
        return algorithm;
    }
}
