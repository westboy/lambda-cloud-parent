package com.lambda.cloud.ykc.protocol.v16.down;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.down.YkcV16WorkingParamsSetDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16WorkingParamsSetRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("52", YkcV16WorkingParamsSetDown.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16WorkingParamsSetDown req = new YkcV16WorkingParamsSetDown();
        req.setEquipmentId("32010200000001");
        req.setAllowWork(1);
        req.setMaxOutputPowerPercent(0);

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("52");
        base.setSerialNumber(8);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("52", parsed.getFrameType());
        assertEquals(13, parsed.getDataLength());
        assertEquals(8, parsed.getSerialNumber());

        YkcV16WorkingParamsSetDown detail = assertInstanceOf(YkcV16WorkingParamsSetDown.class, parsed.getDetail());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getAllowWork());
        assertEquals(0, detail.getMaxOutputPowerPercent());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
