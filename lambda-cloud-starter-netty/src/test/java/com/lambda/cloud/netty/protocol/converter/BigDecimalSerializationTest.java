package com.lambda.cloud.netty.protocol.converter;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.converter.impl.HexConverter;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * BigDecimal序列化测试
 */
public class BigDecimalSerializationTest {

    private final HexConverter hexConverter = new HexConverter();

    /**
     * 测试BigDecimal序列化 - 4位精度，小端序
     */
    @Test
    void testBigDecimalSerialization() throws Exception {
        // 创建测试字段
        Field testField = TestClass.class.getDeclaredField("testAmount");
        ProtocolField protocolField = testField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(testField, protocolField, null);

        BigDecimal testValue = new BigDecimal("4.8200");

        // 序列化
        byte[] result = hexConverter.serialize(testValue, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.length);

        // 期望的字节序列（小端序）：4.8200 * 10000 = 48200 = 0xBC48
        // 小端序：48 BC 00 00
        byte[] expected = {0x48, (byte) 0xBC, 0x00, 0x00};
        assertArrayEquals(expected, result);

        System.out.println("原始值: " + testValue);
        System.out.println("序列化结果: " + bytesToHex(result));
        System.out.println("期望结果: " + bytesToHex(expected));
    }

    /**
     * 测试BigDecimal序列化 - 5位精度，小端序
     */
    @Test
    void testBigDecimalSerializationWithPrecision5() throws Exception {
        // 创建测试字段
        Field testField = TestClass.class.getDeclaredField("testPrice");
        ProtocolField protocolField = testField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(testField, protocolField, null);

        // 测试值：1.23456
        BigDecimal testValue = new BigDecimal("1.23456");

        // 序列化
        byte[] result = hexConverter.serialize(testValue, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.length);

        // 期望的字节序列（小端序）：1.23456 * 100000 = 123456 = 0x1E240
        // 小端序：40 E2 01 00
        byte[] expected = {0x40, (byte) 0xE2, 0x01, 0x00};
        assertArrayEquals(expected, result);

        System.out.println("原始值: " + testValue);
        System.out.println("序列化结果: " + bytesToHex(result));
        System.out.println("期望结果: " + bytesToHex(expected));
    }

    /**
     * 测试往返一致性
     */
    @Test
    void testRoundTripConsistency() throws Exception {
        Field testField = TestClass.class.getDeclaredField("testAmount");
        ProtocolField protocolField = testField.getAnnotation(ProtocolField.class);
        ProtocolFieldMetadata fieldMetadata = new ProtocolFieldMetadata(testField, protocolField, null);

        BigDecimal originalValue = new BigDecimal("4.8200");

        // 序列化
        byte[] serialized = hexConverter.serialize(originalValue, fieldMetadata);

        // 反序列化
        Object parsed = hexConverter.parse(serialized, fieldMetadata);

        // 验证
        assertInstanceOf(BigDecimal.class, parsed);
        BigDecimal parsedValue = (BigDecimal) parsed;

        // 比较值（考虑精度）
        assertEquals(0, originalValue.compareTo(parsedValue));

        System.out.println("原始值: " + originalValue);
        System.out.println("序列化: " + bytesToHex(serialized));
        System.out.println("解析值: " + parsedValue);
        System.out.println("往返测试: " + (originalValue.compareTo(parsedValue) == 0 ? "通过" : "失败"));
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    /**
     * 测试类
     */
    static class TestClass {
        @ProtocolField(
                order = 1,
                length = 4,
                dataType = ProtocolDataType.HEX,
                littleEndian = true,
                precision = 4,
                description = "测试金额")
        private BigDecimal testAmount;

        @ProtocolField(
                order = 2,
                length = 4,
                dataType = ProtocolDataType.HEX,
                littleEndian = true,
                precision = 5,
                description = "测试价格")
        private BigDecimal testPrice;
    }
}
