package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.up.YkcV20LogCallRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20LogCallRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("98", YkcV20LogCallRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20LogCallRequest detail = new YkcV20LogCallRequest();
        detail.setEquipmentId("1234567890123");
        detail.setUploadServerAddress("A".repeat(16));
        detail.setUploadServerPort(300);
        detail.setUsername("A".repeat(16));
        detail.setPassword("A".repeat(16));
        detail.setUploadServerPath("A".repeat(64));
        detail.setStartTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        detail.setEndTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        detail.setLogType(1);
        detail.setLocalLogPath("A".repeat(64));
        detail.setUploadTimeoutMinutes(1);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("98");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("98", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20LogCallRequest parsedDetail = assertInstanceOf(YkcV20LogCallRequest.class, parsed.getDetail());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals("A".repeat(16), parsedDetail.getUploadServerAddress());
        assertEquals(300, parsedDetail.getUploadServerPort());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
