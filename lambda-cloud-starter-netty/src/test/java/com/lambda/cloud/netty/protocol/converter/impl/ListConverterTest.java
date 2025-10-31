package com.lambda.cloud.netty.protocol.converter.impl;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.checksum.ChecksumService;
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
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null, new ChecksumService());
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<BillingModelMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] bytes = {0x09, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};

        ByteBuf byteBuf = Unpooled.wrappedBuffer(HexUtil.decodeHex("0902030405"));

        BillingModelMessage record = engine.parse(byteBuf, BillingModelMessage.class);

        System.out.println(record.getTimeSlotRateNumbers());
        System.out.println(record.getFees());

        ByteBuf serializeBuffer = Unpooled.buffer();
        engine.serialize(record, serializeBuffer);
        byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
        serializeBuffer.readBytes(serializedBytes);
        System.out.println("序列化报文： " + HexUtil.encodeHexStr(serializedBytes, false));
    }
}
