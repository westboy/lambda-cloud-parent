package com.lambda.cloud.ykc.protocol.v20.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.req.YkcV20LoginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20LoginRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("01", YkcV20LoginRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20LoginRequest req = new YkcV20LoginRequest();
        req.setRandomKey("A".repeat(88));
        req.setEquipmentId("55031412782305");
        req.setStationType(0);
        req.setConnectorCount(2);
        req.setProtocolVersion("01000B");
        req.setProgramVersion("V2.0.0");
        req.setNetworkType(1);
        req.setSimCard("01010101010101010101");
        req.setOperator(4);
        req.setToken("00000000000001");
        req.setPhoneNumber("13800138000");
        req.setSupportedNetworkStandards(0x0F);
        req.setCurrentNetworkStandard(0x04);
        req.setLongitude(270000000);
        req.setLatitude(45000000);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("01");
        base.setSerialNumber(3);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("01", parsed.getFrameType());
        assertEquals(3, parsed.getSerialNumber());

        YkcV20LoginRequest detail = assertInstanceOf(YkcV20LoginRequest.class, parsed.getDetail());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(2, detail.getConnectorCount());
        assertEquals("01000b", detail.getProtocolVersion());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
