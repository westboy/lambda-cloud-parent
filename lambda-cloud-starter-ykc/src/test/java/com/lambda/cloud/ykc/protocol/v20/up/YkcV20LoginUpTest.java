package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.up.YkcV20LoginUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20LoginUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("01", YkcV20LoginUp.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20LoginUp req = new YkcV20LoginUp();
        req.setRandomKey("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        req.setEquipmentId("15031412782305");
        req.setStationType(0);
        req.setConnectorCount(2);
        req.setProtocolVersion("01000B");
        req.setProgramVersion("V4.1.50");
        req.setNetworkType(1);
        req.setSimCard("01010101010101010101");
        req.setOperator(4);
        req.setToken("00000000000000");
        req.setPhoneNumber("00000000000");
        req.setSupportedNetworkStandards(15);
        req.setCurrentNetworkStandard(4);
        req.setLongitude(270000000);
        req.setLatitude(45000000);

        YkcV20BasePayload base = new YkcV20BasePayload();
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
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("01", parsed.getFrameType());
        assertEquals(0, parsed.getSerialNumber());

        YkcV20LoginUp detail = assertInstanceOf(YkcV20LoginUp.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals(0, detail.getStationType());
        assertEquals(2, detail.getConnectorCount());
        assertEquals("01000b", detail.getProtocolVersion());
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
