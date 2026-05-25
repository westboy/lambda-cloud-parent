package com.lambda.cloud.netty.protocol.converter.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.impl.ReflectionFieldAccessor;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BcdConverterTest {

    private final BcdConverter converter = new BcdConverter();

    @Test
    void parseFromStringReturnsTypedDefaultForBlankLongField() throws Exception {
        Object value = converter.parseFromString("   ", metadata("longValue"));

        assertEquals(0L, value);
    }

    @Test
    void parseFromStringReturnsTypedDefaultForBlankBigDecimalField() throws Exception {
        Object value = converter.parseFromString(null, metadata("decimalValue"));

        assertEquals(BigDecimal.ZERO, value);
    }

    @Test
    void parseFromStringWrapsOverflowAsProtocolException() throws Exception {
        ProtocolException exception = assertThrows(
                ProtocolException.class,
                () -> converter.parseFromString("999999999999999999999999", metadata("intValue")));

        assertEquals(ProtocolException.ErrorCode.PARSE_ERROR, exception.getErrorCode());
    }

    @Test
    void serializePreservesDigitsWhenTargetLengthIsZero() throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        try {
            converter.serialize("1234", buffer, metadata("rawDigits"));

            byte[] actual = new byte[buffer.readableBytes()];
            buffer.readBytes(actual);
            assertArrayEquals(HexUtil.decodeHex("1234"), actual);
        } finally {
            buffer.release();
        }
    }

    @Test
    void serializeScalesExactBigDecimalWithoutLosingPrecision() throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        try {
            converter.serialize(new BigDecimal("12.34"), buffer, metadata("decimalValue"));

            byte[] actual = new byte[buffer.readableBytes()];
            buffer.readBytes(actual);
            assertArrayEquals(HexUtil.decodeHex("1234"), actual);
        } finally {
            buffer.release();
        }
    }

    @Test
    void serializeRejectsScaledBigDecimalWithFractionalRemainder() throws Exception {
        ProtocolException exception = assertThrows(
                ProtocolException.class,
                () -> converter.serialize(new BigDecimal("12.345"), Unpooled.buffer(), metadata("rawDecimal")));

        assertEquals(ProtocolException.ErrorCode.SERIALIZE_ERROR, exception.getErrorCode());
        assertInstanceOf(ArithmeticException.class, exception.getCause());
    }

    private ProtocolFieldMetadata metadata(String fieldName) throws NoSuchFieldException {
        Field field = TestFields.class.getDeclaredField(fieldName);
        return new ProtocolFieldMetadata(
                new ReflectionFieldAccessor(field), field.getAnnotation(ProtocolField.class), null, null);
    }

    private static class TestFields {
        @ProtocolField(order = 0, dataType = ProtocolDataType.BCD, length = 2)
        private Long longValue;

        @ProtocolField(order = 1, dataType = ProtocolDataType.BCD, length = 2)
        private Integer intValue;

        @ProtocolField(order = 2, dataType = ProtocolDataType.BCD, length = 2, precision = 2)
        private BigDecimal decimalValue;

        @ProtocolField(order = 3, dataType = ProtocolDataType.BCD, length = 0)
        private String rawDigits;

        @ProtocolField(order = 4, dataType = ProtocolDataType.BCD, length = 0, precision = 2)
        private BigDecimal rawDecimal;
    }
}
