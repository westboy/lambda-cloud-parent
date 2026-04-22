package com.lambda.cloud.netty.protocol.billing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ListConverter单元测试
 *
 * @author zx
 */
@ExtendWith(MockitoExtension.class)
class ListConverterTest {

    @Test
    void test0() throws ProtocolException {
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<BillingModelMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] raw = HexUtil.decodeHex("0102090203");
        ByteBuf byteBuf = Unpooled.wrappedBuffer(raw);

        BillingModelMessage record = engine.parse(byteBuf, BillingModelMessage.class);
        assertEquals(1, record.getFees().size());
        assertEquals(3, record.getTimeSlotRateNumbers().size());

        ByteBuf serializeBuffer = Unpooled.buffer();
        engine.serialize(record, serializeBuffer);
        byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
        serializeBuffer.readBytes(serializedBytes);
        assertArrayEquals(raw, serializedBytes);
    }
}
