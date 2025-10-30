package com.lambda.cloud.netty.protocol.converter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessorFactory;
import com.lambda.cloud.netty.protocol.accessor.FileAccessorType;
import com.lambda.cloud.netty.protocol.annotation.PaddingDirection;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.checksum.ChecksumService;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ListConverter单元测试
 *
 * @author zx
 */
@ExtendWith(MockitoExtension.class)
class ListConverterTest {

    @Mock
    private DataTypeConverterFactory converterFactory;

    private ListConverter listConverter;

    @BeforeEach
    void setUp() {
        listConverter = new ListConverter(converterFactory);
    }

    @Test
    void test0() throws ProtocolException {
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null, new ChecksumService());
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<BillingModelMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] bytes = {0x09, 0x02, 0x03, 0x04, 0x05, 0x05};

        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

        BillingModelMessage record = engine.parse(byteBuf, BillingModelMessage.class);

        System.out.println("解析结果: " + record);
        System.out.println("fees字段 (listLength=2): " + record.getFees());
        if (record.getFees() != null) {
            System.out.println("fees字段大小: " + record.getFees().size());
            for (int i = 0; i < record.getFees().size(); i++) {
                BillingModelFee fee = record.getFees().get(i);
                System.out.println("  fees[" + i + "]: electricityRate=" + fee.getElectricityRate() + ", serviceRate="
                        + fee.getServiceRate());
            }
        }
        System.out.println("timeSlotRateNumbers字段 (listElementLength=2): " + record.getTimeSlotRateNumbers());
        if (record.getTimeSlotRateNumbers() != null) {
            System.out.println("timeSlotRateNumbers字段大小: "
                    + record.getTimeSlotRateNumbers().size());
            for (int i = 0; i < record.getTimeSlotRateNumbers().size(); i++) {
                System.out.println("  timeSlotRateNumbers[" + i + "]: "
                        + record.getTimeSlotRateNumbers().get(i));
            }
        }
    }

    /**
     * 测试固定长度UINT8 List的解析
     */
    @Test
    void testParseFixedLengthUInt8List() throws Exception {
        // 准备测试数据
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 5, 0, 1);

        // Mock转换器
        UInt8Converter uint8Converter = new UInt8Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT8)).thenReturn(uint8Converter);

        // 执行解析
        Object result = listConverter.parse(data, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(4, list.get(3));
        assertEquals(5, list.get(4));
    }

    /**
     * 测试固定长度UINT16 List的解析
     */
    @Test
    void testParseFixedLengthUInt16List() throws Exception {
        // 准备测试数据 (大端序)
        byte[] data = {0x00, 0x01, 0x00, 0x02, 0x00, 0x03};
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT16, 3, 0, 2);

        // Mock转换器
        UInt16Converter uint16Converter = new UInt16Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT16)).thenReturn(uint16Converter);

        // 执行解析
        Object result = listConverter.parse(data, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    /**
     * 测试动态长度List的解析（根据数据长度计算）
     */
    @Test
    void testParseDynamicLengthList() throws Exception {
        // 准备测试数据
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        ProtocolFieldMetadata fieldMetadata =
                createListFieldMetadata(ProtocolDataType.UINT8, 0, 0, 1); // 不指定listLength，根据数据长度计算

        // Mock转换器
        UInt8Converter uint8Converter = new UInt8Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT8)).thenReturn(uint8Converter);

        // 执行解析
        Object result = listConverter.parse(data, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(4, list.size());
    }

    /**
     * 测试List序列化
     */
    @Test
    void testSerializeList() throws Exception {
        // 准备测试数据
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 5, 0, 1);

        // Mock转换器
        UInt8Converter uint8Converter = new UInt8Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT8)).thenReturn(uint8Converter);

        // 执行序列化
        byte[] result = listConverter.serialize(list, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
        assertEquals(4, result[3]);
        assertEquals(5, result[4]);
    }

    /**
     * 测试空List序列化
     */
    @Test
    void testSerializeEmptyList() throws Exception {
        // 准备测试数据
        List<Integer> list = Arrays.asList();
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 0, 0, 1);

        // 执行序列化
        byte[] result = listConverter.serialize(list, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * 测试null值序列化
     */
    @Test
    void testSerializeNullValue() throws Exception {
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 0, 0, 1);

        // 执行序列化
        byte[] result = listConverter.serialize(null, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * 测试从字符串解析List
     */
    @Test
    void testParseFromString() throws Exception {
        // 准备测试数据
        String value = "1,2,3,4,5";
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 5, 0, 1);

        // Mock转换器
        UInt8Converter uint8Converter = new UInt8Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT8)).thenReturn(uint8Converter);

        // 执行解析
        Object result = listConverter.parseFromString(value, fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(4, list.get(3));
        assertEquals(5, list.get(4));
    }

    /**
     * 测试从空字符串解析List
     */
    @Test
    void testParseFromEmptyString() throws Exception {
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 0, 0, 1);

        // 执行解析
        Object result = listConverter.parseFromString("", fieldMetadata);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(0, list.size());
    }

    /**
     * 测试非List字段解析异常
     */
    @Test
    void testParseNonListFieldException() throws Exception {
        byte[] data = {0x01, 0x02};
        ProtocolFieldMetadata fieldMetadata = createNonListFieldMetadata();

        // 执行并验证异常
        ProtocolException exception = assertThrows(ProtocolException.class, () -> {
            listConverter.parse(data, fieldMetadata);
        });

        assertEquals(ProtocolException.ErrorCode.PARSE_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("字段不是List类型"));
    }

    /**
     * 测试非List值序列化异常
     */
    @Test
    void testSerializeNonListValueException() throws Exception {
        String value = "not a list";
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 0, 0, 1);

        // 执行并验证异常
        ProtocolException exception = assertThrows(ProtocolException.class, () -> {
            listConverter.serialize(value, fieldMetadata);
        });

        assertEquals(ProtocolException.ErrorCode.SERIALIZE_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("值不是List类型"));
    }

    /**
     * 测试数据不足异常
     */
    @Test
    void testParseInsufficientDataException() throws Exception {
        byte[] data = {0x01, 0x02}; // 只有2字节
        ProtocolFieldMetadata fieldMetadata = createListFieldMetadata(ProtocolDataType.UINT8, 5, 0, 1); // 期望5个元素

        // Mock转换器
        UInt8Converter uint8Converter = new UInt8Converter();
        when(converterFactory.getConverter(ProtocolDataType.UINT8)).thenReturn(uint8Converter);

        // 执行并验证异常
        ProtocolException exception = assertThrows(ProtocolException.class, () -> {
            listConverter.parse(data, fieldMetadata);
        });

        assertEquals(ProtocolException.ErrorCode.PARSE_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("数据不足"));
    }

    /**
     * 创建List字段元数据
     */
    private ProtocolFieldMetadata createListFieldMetadata(
            ProtocolDataType elementType, int listLength, int listLengthField, int elementLength) throws Exception {
        // 创建真实字段
        Field testField = TestMessage.class.getDeclaredField("testList");
        FieldAccessor realFieldAccessor = FieldAccessorFactory.createAccessor(testField, FileAccessorType.REFLECTION);

        // 创建Mock注解
        ProtocolField mockAnnotation = mock(ProtocolField.class, withSettings().lenient());
        when(mockAnnotation.dataType()).thenReturn(ProtocolDataType.LIST);
        when(mockAnnotation.listElementType()).thenReturn(elementType);
        when(mockAnnotation.listLength()).thenReturn(listLength);
        when(mockAnnotation.listElementLength()).thenReturn(elementLength);
        when(mockAnnotation.order()).thenReturn(1);
        when(mockAnnotation.composite()).thenReturn(false);
        when(mockAnnotation.listLengthField()).thenReturn("");

        // 添加其他可能被调用的方法的默认值
        when(mockAnnotation.length()).thenReturn(0);
        when(mockAnnotation.precision()).thenReturn(0);
        when(mockAnnotation.littleEndian()).thenReturn(false);
        when(mockAnnotation.description()).thenReturn("");
        when(mockAnnotation.optional()).thenReturn(false);
        when(mockAnnotation.defaultValue()).thenReturn("");
        when(mockAnnotation.charset()).thenReturn("UTF-8");
        when(mockAnnotation.padding()).thenReturn(PaddingDirection.LEFT);
        when(mockAnnotation.paddingChar()).thenReturn("0");
        when(mockAnnotation.encryptedKey()).thenReturn(false);
        when(mockAnnotation.encryptedField()).thenReturn(false);
        when(mockAnnotation.computed()).thenReturn(false);
        when(mockAnnotation.crcFiled()).thenReturn(false);
        when(mockAnnotation.lengthFiled()).thenReturn(false);
        when(mockAnnotation.serialFiled()).thenReturn(false);

        return new ProtocolFieldMetadata(realFieldAccessor, mockAnnotation, null);
    }

    /**
     * 创建非List字段元数据
     */
    private ProtocolFieldMetadata createNonListFieldMetadata() throws Exception {
        // 创建真实字段
        Field testField = TestMessage.class.getDeclaredField("testValue");
        FieldAccessor realFieldAccessor = FieldAccessorFactory.createAccessor(testField, FileAccessorType.REFLECTION);

        // 创建Mock注解
        ProtocolField mockAnnotation = mock(ProtocolField.class, withSettings().lenient());
        when(mockAnnotation.dataType()).thenReturn(ProtocolDataType.UINT8);
        when(mockAnnotation.order()).thenReturn(1);
        when(mockAnnotation.composite()).thenReturn(false);

        // 添加其他可能被调用的方法的默认值
        when(mockAnnotation.length()).thenReturn(0);
        when(mockAnnotation.precision()).thenReturn(0);
        when(mockAnnotation.littleEndian()).thenReturn(false);
        when(mockAnnotation.description()).thenReturn("");
        when(mockAnnotation.optional()).thenReturn(false);
        when(mockAnnotation.defaultValue()).thenReturn("");
        when(mockAnnotation.charset()).thenReturn("UTF-8");
        when(mockAnnotation.padding()).thenReturn(PaddingDirection.LEFT);
        when(mockAnnotation.paddingChar()).thenReturn("0");
        when(mockAnnotation.encryptedKey()).thenReturn(false);
        when(mockAnnotation.encryptedField()).thenReturn(false);
        when(mockAnnotation.computed()).thenReturn(false);
        when(mockAnnotation.crcFiled()).thenReturn(false);
        when(mockAnnotation.lengthFiled()).thenReturn(false);
        when(mockAnnotation.serialFiled()).thenReturn(false);
        when(mockAnnotation.listElementType()).thenReturn(ProtocolDataType.HEX);
        when(mockAnnotation.listLength()).thenReturn(0);
        when(mockAnnotation.listLengthField()).thenReturn("");
        when(mockAnnotation.listElementLength()).thenReturn(0);

        return new ProtocolFieldMetadata(realFieldAccessor, mockAnnotation, null);
    }

    /**
     * 测试消息类
     */
    private static class TestMessage {
        private List<Integer> testList;
        private Integer testValue;
    }
}
