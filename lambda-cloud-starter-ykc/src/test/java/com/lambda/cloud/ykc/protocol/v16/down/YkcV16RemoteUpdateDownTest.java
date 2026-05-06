package com.lambda.cloud.ykc.protocol.v16.down;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.down.YkcV16RemoteUpdateDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16RemoteUpdateDownTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("94", YkcV16RemoteUpdateDown.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16RemoteUpdateDown req = new YkcV16RemoteUpdateDown();
        req.setEquipmentId("55031412782305");
        req.setEquipmentModel(1);
        req.setEquipmentPower(15);
        req.setServerAddress("114.55.114.174");
        req.setServerPort(21);
        req.setUsername("sr");
        req.setPassword("sr123");
        req.setFilePath("AC-7KW/20180131");
        req.setExecuteControl(2);
        req.setDownloadTimeoutMin(60);

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("94");
        base.setSerialNumber(38);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("94", parsed.getFrameType());
        assertEquals(38, parsed.getSerialNumber());

        YkcV16RemoteUpdateDown detail = assertInstanceOf(YkcV16RemoteUpdateDown.class, parsed.getDetail());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getEquipmentModel());
        assertEquals(15, detail.getEquipmentPower());
        assertTrue(detail.getServerAddress().startsWith("114.55.114.174"));
        assertEquals(21, detail.getServerPort());
        assertEquals("sr", detail.getUsername());
        assertTrue(detail.getPassword().startsWith("sr123"));
        assertTrue(detail.getFilePath().startsWith("AC-7KW/20180131"));
        assertEquals(2, detail.getExecuteControl());
        assertEquals(60, detail.getDownloadTimeoutMin());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
