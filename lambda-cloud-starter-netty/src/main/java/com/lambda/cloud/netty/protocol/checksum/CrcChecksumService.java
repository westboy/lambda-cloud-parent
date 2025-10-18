package com.lambda.cloud.netty.protocol.checksum;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
import com.lambda.cloud.netty.exception.ProtocolException;
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
        registerAlgorithm("CRC16-CCITT", Crc16Algorithm.ccitt());
        registerAlgorithm("CRC16-IBM", Crc16Algorithm.ibm());
        registerAlgorithm("CRC16-MAXIM", Crc16Algorithm.maxim());
        registerAlgorithm("CRC16-USB", Crc16Algorithm.usb());
        registerAlgorithm("CRC16-X25", Crc16Algorithm.x25());
        registerAlgorithm("CRC16-XMODEM", Crc16Algorithm.xmodem());
        registerAlgorithm("CRC16-MODBUS", Crc16Algorithm.modbus());
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
}
