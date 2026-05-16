package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.down.YkcV20LogCallDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20LogCallDownTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("98", YkcV20LogCallDown.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20LogCallDown resp = new YkcV20LogCallDown();
        resp.setEquipmentId("15031412782305");
        resp.setUploadServerAddress("192.168.1.1");
        resp.setUploadServerPort(21);
        resp.setUsername("admin");
        resp.setPassword("admin123");
        resp.setUploadServerPath("/logs/");
        resp.setLogStartTime("2025-01-01 12:00:00");
        resp.setLogEndTime("2025-01-01 12:00:00");
        resp.setLogType(0);
        resp.setChargerLogFilePath("");
        resp.setUploadTimeout(10);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("98");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("98", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20LogCallDown detail = assertInstanceOf(YkcV20LogCallDown.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals(21, detail.getUploadServerPort());
        assertEquals(0, detail.getLogType());
        assertEquals(10, detail.getUploadTimeout());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
