package com.lambda.cloud.ykc.protocol.v16.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.req.YkcV16LoginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16LoginRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("01", YkcV16LoginRequest.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16LoginRequest req = new YkcV16LoginRequest();
        req.setEquipmentId("55031412782305");
        req.setEquipmentType(0);
        req.setConnectorCount(2);
        req.setProtocolVersion(15);
        req.setProgramVersion("V4.1.50");
        req.setNetworkType(1);
        req.setSimCardNumber("01010101010101010101");
        req.setOperator(4);

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("01");
        base.setSerialNumber(0);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("01", parsed.getFrameType());
        assertEquals(0, parsed.getSerialNumber());

        YkcV16LoginRequest detail = assertInstanceOf(YkcV16LoginRequest.class, parsed.getDetail());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(0, detail.getEquipmentType());
        assertEquals(2, detail.getConnectorCount());
        assertEquals(15, detail.getProtocolVersion());
        assertEquals("V4.1.50", detail.getProgramVersion().substring(0, 7));
        assertEquals(1, detail.getNetworkType());
        assertEquals(4, detail.getOperator());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
