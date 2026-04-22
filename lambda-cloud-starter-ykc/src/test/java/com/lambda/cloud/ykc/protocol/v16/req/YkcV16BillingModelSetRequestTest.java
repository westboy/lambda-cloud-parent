package com.lambda.cloud.ykc.protocol.v16.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.req.YkcV16BillingModelSetRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16BillingModelSetRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("58", YkcV16BillingModelSetRequest.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16BillingModelSetRequest req = new YkcV16BillingModelSetRequest();
        req.setEquipmentId("55031412782305");
        req.setBillingModelCode("0100");
        req.setPeakElectricityRate(new BigDecimal("2.00000"));
        req.setPeakServiceRate(new BigDecimal("0.40000"));
        req.setHighElectricityRate(new BigDecimal("2.00000"));
        req.setHighServiceRate(new BigDecimal("0.40000"));
        req.setNormalElectricityRate(new BigDecimal("4.00000"));
        req.setNormalServiceRate(new BigDecimal("0.40000"));
        req.setValleyElectricityRate(new BigDecimal("5.00000"));
        req.setValleyServiceRate(new BigDecimal("0.40000"));
        req.setLossRatio(0);
        req.setTimeSlotRates(Collections.nCopies(48, 0));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("58");
        base.setSerialNumber(37);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("58", parsed.getFrameType());
        assertEquals(37, parsed.getSerialNumber());

        YkcV16BillingModelSetRequest detail = assertInstanceOf(YkcV16BillingModelSetRequest.class, parsed.getDetail());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals("0100", detail.getBillingModelCode());
        assertEquals(48, detail.getTimeSlotRates().size());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
