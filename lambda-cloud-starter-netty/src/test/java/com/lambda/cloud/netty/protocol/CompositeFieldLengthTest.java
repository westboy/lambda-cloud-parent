package com.lambda.cloud.netty.protocol;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 复合字段长度测试
 */
public class CompositeFieldLengthTest {

    private ProtocolEngine protocolEngine;

    @BeforeEach
    void setUp() {
        protocolEngine = new ReflectionProtocolEngine();
    }

    /**
     * 测试复合字段长度为0的情况
     */
    @Test
    void testCompositeFieldWithZeroLength() throws Exception {
        // 创建测试消息
        TestMessage message = new TestMessage();
        message.setHeader((byte) 0x01);
        message.setData(new TestData());
        message.getData().setField1((byte) 0x02);
        message.getData().setField2((byte) 0x03);

        message.setTail((byte) 0x04);

        // 序列化
        ByteBuf serialized = Unpooled.buffer();
        protocolEngine.serialize(message, serialized);
        assertNotNull(serialized);

        // 打印序列化后的数据
        byte[] bytes = new byte[serialized.readableBytes()];
        serialized.getBytes(0, bytes);
        System.out.println("序列化数据: " + bytesToHex(bytes));
        System.out.println("数据长度: " + bytes.length);

        // 解析
        TestMessage parsed = (TestMessage) protocolEngine.parse(serialized, TestMessage.class);
        assertNotNull(parsed);

        // 验证基本字段
        assertEquals(message.getHeader(), parsed.getHeader());
        assertEquals(message.getTail(), parsed.getTail());

        // 验证复合字段 - 这里应该会失败
        System.out.println("解析后的复合字段: " + parsed.getData());
        assertNotNull(parsed.getData(), "复合字段不应该为null");
    }

    /**
     * 测试复合字段长度为正确值的情况
     */
    @Test
    void testCompositeFieldWithCorrectLength() throws Exception {
        // 创建测试消息
        TestMessage2 message = new TestMessage2();
        message.setHeader((byte) 0x01);
        message.setData(new TestData());
        message.getData().setField1((byte) 0x02);
        message.getData().setField2((byte) 0x03);

        message.setTail((byte) 0x04);

        // 序列化
        ByteBuf serialized = Unpooled.buffer();
        protocolEngine.serialize(message, serialized);
        assertNotNull(serialized);

        // 打印序列化后的数据
        byte[] bytes = new byte[serialized.readableBytes()];
        serialized.getBytes(0, bytes);
        System.out.println("序列化数据: " + bytesToHex(bytes));
        System.out.println("数据长度: " + bytes.length);

        // 解析
        TestMessage2 parsed = (TestMessage2) protocolEngine.parse(serialized, TestMessage2.class);
        assertNotNull(parsed);

        // 验证所有字段
        assertEquals(message.getHeader(), parsed.getHeader());
        assertEquals(message.getTail(), parsed.getTail());
        assertNotNull(parsed.getData(), "复合字段不应该为null");
        assertEquals(message.getData().getField1(), parsed.getData().getField1());
        assertEquals(message.getData().getField2(), parsed.getData().getField2());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Data
    @ProtocolFrame
    public static class TestMessage {
        @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX)
        private byte header;

        @ProtocolField(order = 1, composite = true, length = 0) // 长度为0
        private TestData data;

        @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.HEX)
        private byte tail;
    }

    @Data
    @ProtocolFrame
    public static class TestMessage2 {
        @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX)
        private byte header;

        @ProtocolField(order = 1, composite = true, length = 2) // 正确的长度
        private TestData data;

        @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.HEX)
        private byte tail;
    }

    @Data
    @ProtocolFrame
    public static class TestData {
        @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX)
        private byte field1;

        @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX)
        private byte field2;
    }
}
