package com.lambda.cloud.netty.protocol.checksum;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
import org.junit.jupiter.api.Test;

/**
 * 简单的CRC功能测试
 */
public class SimpleCrcTest {

    @Test
    public void testCrc16Algorithms() {
        // 测试CRC16算法实例化
        assertNotNull(Crc16Algorithm.ccitt());
        assertNotNull(Crc16Algorithm.ibm());
        assertNotNull(Crc16Algorithm.maxim());
        assertNotNull(Crc16Algorithm.usb());
        assertNotNull(Crc16Algorithm.x25());
        assertNotNull(Crc16Algorithm.xmodem());

        System.out.println("所有CRC16算法实例化成功");
    }


    @Test
    public void testCrcCalculation() {
        // 测试基本的CRC计算
        Crc16Algorithm crc16 = Crc16Algorithm.ccitt();
        byte[] testData = "Hello World".getBytes();

        long result = crc16.calculate(testData);
        assertTrue(result >= 0);

        System.out.println("CRC16计算结果: " + result);
    }

    @Test
    public void testCrcService() {
        // 测试CRC服务
        CrcChecksumService service = new CrcChecksumService();
        assertNotNull(service);

        System.out.println("CRC服务实例化成功");
    }
}