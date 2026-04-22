package com.lambda.cloud.ykc.protocol.v20.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.req.YkcV20VinCodeReportRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20VinCodeReportRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A9", YkcV20VinCodeReportRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20VinCodeReportRequest detail = new YkcV20VinCodeReportRequest();
        detail.setEquipmentId("1234567890123");
        detail.setConnectorId("1");
        detail.setVinCode("1HGCM82633A004352");
        detail.setCurrentTime(LocalDateTime.of(2025, 1, 2, 3, 4, 5));

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A9");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("A9", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20VinCodeReportRequest parsedDetail =
                assertInstanceOf(YkcV20VinCodeReportRequest.class, parsed.getDetail());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals("1HGCM82633A004352", parsedDetail.getVinCode());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
