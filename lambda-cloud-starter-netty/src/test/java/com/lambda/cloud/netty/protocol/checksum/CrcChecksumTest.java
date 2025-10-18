package com.lambda.cloud.netty.protocol.checksum;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.checksum.impl.Crc16Algorithm;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.exception.ProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CRC校验测试类
 * 测试CRC算法、转换器和协议引擎集成
 *
 * @author lambda
 * @since 2024-01-01
 */
public class CrcChecksumTest {

    private ProtocolEngine protocolEngine;
    private CrcChecksumService crcService;

    @BeforeEach
    void setUp() {
        protocolEngine = new ReflectionProtocolEngine();
        crcService = new CrcChecksumService();
    }

    @Test
    void testCrcChecksumService() throws ProtocolException {

        // 获取算法
        CrcAlgorithm algorithm = crcService.getAlgorithm("CRC16_CCITT");
        assertNotNull(algorithm);
        assertEquals("CRC16_CCITT", algorithm.algorithmName());
        assertEquals(2, algorithm.getChecksumLength());
    }

    @Test
    void testProtocolEngineWithCrc() throws ProtocolException {
        // 创建测试消息
        CrcTestMessage message = new CrcTestMessage();
        message.setHeader("AA55");
        message.setMessageType(0x01);
        message.setDataLength(10);
        message.setData("1234567890ABCDEF1234");

        // 序列化消息（应该自动计算CRC）
        ByteBuf buffer = Unpooled.buffer();
        protocolEngine.serialize(message, buffer);

        // 验证缓冲区有数据
        assertTrue(buffer.readableBytes() > 0);

        // 解析消息（应该自动验证CRC）
        buffer.readerIndex(0);
        CrcTestMessage parsedMessage = (CrcTestMessage) protocolEngine.parse(buffer, CrcTestMessage.class);

        // 验证解析结果
        assertNotNull(parsedMessage);
        assertEquals(message.getHeader(), parsedMessage.getHeader());
        assertEquals(message.getMessageType(), parsedMessage.getMessageType());
        assertEquals(message.getDataLength(), parsedMessage.getDataLength());
        assertEquals(message.getData(), parsedMessage.getData());

        // CRC应该被自动计算和设置
        assertTrue(parsedMessage.getCrc() > 0);

        buffer.release();
    }

    @Test
    void testCrcValidationFailure() throws ProtocolException {
        // 创建测试消息
        CrcTestMessage message = new CrcTestMessage();

        // 序列化消息
        ByteBuf buffer = Unpooled.buffer();
        protocolEngine.serialize(message, buffer);

        // 修改缓冲区中的CRC值（破坏CRC）
        int bufferLength = buffer.readableBytes();
        if (bufferLength >= 2) {
            buffer.setByte(bufferLength - 2, 0xFF);
            buffer.setByte(bufferLength - 1, 0xFF);
        }

        // 尝试解析应该失败
        buffer.readerIndex(0);
        assertThrows(ProtocolException.class, () -> {
            protocolEngine.parse(buffer, CrcTestMessage.class);
        });

        buffer.release();
    }

    @Test
    void testMultipleCrcAlgorithms() throws ProtocolException {
        // 测试多种CRC算法
        String[] algorithms = {"CRC16_CCITT", "CRC16_IBM", "CRC16_MAXIM", "CRC16_USB", "CRC16_X25", "CRC16_XMODEM"};
        byte[] testData = {0x12, 0x34, 0x56, 0x78};

        for (String algorithmName : algorithms) {
            CrcAlgorithm algorithm = crcService.getAlgorithm(algorithmName);
            assertNotNull(algorithm, "算法 " + algorithmName + " 应该存在");

            int crcValue = Math.toIntExact(algorithm.calculate(testData));
            assertTrue(algorithm.verify(testData, crcValue), "算法 " + algorithmName + " 验证应该成功");
        }
    }

    @Test
    void testCrcEndianness() {
        // 测试字节序处理
        Crc16Algorithm crc16 = new Crc16Algorithm();
        int crcValue = 0x1234;

        // 测试大端序
        byte[] bigEndianBytes = crc16.toBytes(crcValue, false);
        assertEquals(0x12, bigEndianBytes[0] & 0xFF);
        assertEquals(0x34, bigEndianBytes[1] & 0xFF);
        assertEquals(crcValue, crc16.fromBytes(bigEndianBytes, false));

        // 测试小端序
        byte[] littleEndianBytes = crc16.toBytes(crcValue, true);
        assertEquals(0x34, littleEndianBytes[0] & 0xFF);
        assertEquals(0x12, littleEndianBytes[1] & 0xFF);
        assertEquals(crcValue, crc16.fromBytes(littleEndianBytes, true));
    }
}
