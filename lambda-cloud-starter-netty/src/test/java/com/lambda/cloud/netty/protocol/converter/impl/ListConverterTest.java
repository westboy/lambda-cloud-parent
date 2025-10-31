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

        System.out.println(record);
    }
}
