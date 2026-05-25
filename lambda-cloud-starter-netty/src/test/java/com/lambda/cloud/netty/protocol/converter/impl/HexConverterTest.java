package com.lambda.cloud.netty.protocol.converter.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.impl.ReflectionFieldAccessor;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class HexConverterTest {

    private final HexConverter converter = new HexConverter();

    @Test
    void parseFromStringNormalizesPrefixWhitespaceAndOddLength() throws Exception {
        Object value = converter.parseFromString(" 0x1 ", metadata("intValue"));

        assertEquals(1, value);
    }

    @Test
    void serializePreservesBytesWhenTargetLengthIsZero() throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        try {
            converter.serialize("1234", buffer, metadata("rawHex"));

            byte[] actual = new byte[buffer.readableBytes()];
            buffer.readBytes(actual);
            assertArrayEquals(HexUtil.decodeHex("1234"), actual);
        } finally {
            buffer.release();
        }
    }

    @Test
    void roundTripRestoresNegativeIntegerFromTwoComplement() throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        try {
            converter.serialize(-1, buffer, metadata("signedByteValue"));

            Object value = converter.parse(buffer, buffer.readableBytes(), metadata("signedByteValue"));
            assertEquals(-1, value);
        } finally {
            buffer.release();
        }
    }

    private ProtocolFieldMetadata metadata(String fieldName) throws NoSuchFieldException {
        Field field = TestFields.class.getDeclaredField(fieldName);
        return new ProtocolFieldMetadata(
                new ReflectionFieldAccessor(field), field.getAnnotation(ProtocolField.class), null, null);
    }

    private static class TestFields {
        @ProtocolField(order = 0, dataType = ProtocolDataType.HEX, length = 1)
        private Integer intValue;

        @ProtocolField(order = 1, dataType = ProtocolDataType.HEX, length = 0)
        private String rawHex;

        @ProtocolField(order = 2, dataType = ProtocolDataType.HEX, length = 1)
        private Integer signedByteValue;
    }
}
