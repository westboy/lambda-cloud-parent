package com.lambda.cloud.netty.protocol.converter.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import java.lang.reflect.Field;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 复合字段转换器测试
 *
 * @author Jin
 */
class CompositeConverterTest {

    private CompositeConverter compositeConverter;

    @BeforeEach
    void setUp() {
        ReflectionProtocolEngine protocolEngine = new ReflectionProtocolEngine();
        compositeConverter = new CompositeConverter(protocolEngine);
    }

    /**
     * 测试复合字段解析
     */
    @Test
    void testCompositeFieldParsing() throws Exception {
        byte[] testData = {0x01, 0x02, 0x03, 0x04};

        Field compositeField = TestMessage.class.getDeclaredField("compositeField");
        ProtocolField protocolField = compositeField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(compositeField, protocolField, null);

        Object result = compositeConverter.parse(testData, fieldMetadata);

        assertNotNull(result);
        assertInstanceOf(CompositeData.class, result);
    }

    /**
     * 测试复合字段序列化
     */
    @Test
    void testCompositeFieldSerialization() throws Exception {
        CompositeData compositeData = new CompositeData();
        compositeData.setField1((byte) 0x01);
        compositeData.setField2((byte) 0x02);

        Field compositeField = TestMessage.class.getDeclaredField("compositeField");
        ProtocolField protocolField = compositeField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(compositeField, protocolField, null);

        byte[] result = compositeConverter.serialize(compositeData, fieldMetadata);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试从字符串解析（应该抛出异常）
     */
    @Test
    void testParseFromStringThrowsException() throws Exception {
        Field compositeField = TestMessage.class.getDeclaredField("compositeField");
        ProtocolField protocolField = compositeField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(compositeField, protocolField, null);

        assertThrows(ProtocolException.class, () -> {
            compositeConverter.parseFromString("test", fieldMetadata);
        });
    }

    /**
     * 测试消息类
     */
    @Data
    @ProtocolFrame(name = "测试消息", description = "用于测试复合字段的消息")
    public static class TestMessage {
        @ProtocolField(order = 1, length = 4, composite = true)
        private CompositeData compositeField;

        public TestMessage() {
            // 默认构造函数
        }
    }

    /**
     * 复合数据类
     */
    @Data
    @ProtocolFrame(name = "复合数据", description = "复合字段内部数据结构")
    public static class CompositeData {
        @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.UINT8)
        private byte field1;

        @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8)
        private byte field2;

        @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16)
        private int field3;

        public CompositeData() {
            // 默认构造函数
        }
    }
}
