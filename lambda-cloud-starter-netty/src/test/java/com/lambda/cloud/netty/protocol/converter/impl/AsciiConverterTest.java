package com.lambda.cloud.netty.protocol.converter.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.impl.ReflectionFieldAccessor;
import com.lambda.cloud.netty.protocol.annotation.PaddingDirection;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AsciiConverterTest {

    private final AsciiConverter converter = new AsciiConverter();

    @Test
    void serializeTruncatesUtf8WithoutBreakingMultibyteCharacters() throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        try {
            converter.serialize("中文", buffer, metadata("utf8FixedLength"));

            byte[] actual = new byte[buffer.readableBytes()];
            buffer.readBytes(actual);
            assertArrayEquals("中".getBytes(StandardCharsets.UTF_8), actual);
        } finally {
            buffer.release();
        }
    }

    @Test
    void parseFallsBackToUtf8WhenCharsetConfigurationIsInvalid() throws Exception {
        byte[] data = "A".getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        try {
            Object value = converter.parse(buffer, data.length, metadata("invalidCharset"));

            assertEquals("A", value);
        } finally {
            buffer.release();
        }
    }

    @Test
    void parseRemovesConfiguredLeftPaddingCharacters() throws Exception {
        byte[] data = "0012".getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        try {
            Object value = converter.parse(buffer, data.length, metadata("leftPaddedText"));

            assertEquals("12", value);
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
        @ProtocolField(order = 0, dataType = ProtocolDataType.ASCII, length = 3, charset = "UTF-8")
        private String utf8FixedLength;

        @ProtocolField(order = 1, dataType = ProtocolDataType.ASCII, length = 1, charset = "bad-charset")
        private String invalidCharset;

        @ProtocolField(
                order = 2,
                dataType = ProtocolDataType.ASCII,
                length = 4,
                padding = PaddingDirection.LEFT,
                paddingChar = "0")
        private String leftPaddedText;
    }
}
