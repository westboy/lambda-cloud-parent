package com.lambda.cloud.ykc.protocol.v16.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.down.YkcV16BillingModelDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16BillingModelUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("0A", YkcV16BillingModelDown.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16BillingModelDown resp = new YkcV16BillingModelDown();
        resp.setEquipmentId("55031412782305");
        resp.setBillingModelCode("0100");
        resp.setPeakElectricityRate(new BigDecimal("2.00000"));
        resp.setPeakServiceRate(new BigDecimal("0.40000"));
        resp.setHighElectricityRate(new BigDecimal("2.00000"));
        resp.setHighServiceRate(new BigDecimal("0.40000"));
        resp.setNormalElectricityRate(new BigDecimal("4.00000"));
        resp.setNormalServiceRate(new BigDecimal("0.40000"));
        resp.setValleyElectricityRate(new BigDecimal("5.00000"));
        resp.setValleyServiceRate(new BigDecimal("0.40000"));
        resp.setLossRatio(0);
        resp.setTimeSlotRates(Collections.nCopies(48, 0));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("0A");
        base.setSerialNumber(2);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals(2, parsed.getSerialNumber());

        YkcV16BillingModelDown detail = assertInstanceOf(YkcV16BillingModelDown.class, parsed.getDetail());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals("100", detail.getBillingModelCode());
        assertEquals(0, detail.getLossRatio());
        assertEquals(48, detail.getTimeSlotRates().size());
        assertEquals(0, detail.getTimeSlotRates().getFirst());
        assertEquals(0, detail.getTimeSlotRates().get(47));

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
