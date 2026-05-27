package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.down.YkcV20BillingModelSetDown;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20BillingModelSetDownTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("58", YkcV20BillingModelSetDown.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20BillingModelSetDown resp = new YkcV20BillingModelSetDown();
        resp.setEquipmentId("15031412782305");
        resp.setBillingModelNumber("0100");
        resp.setFeeCount(2);

        List<YkcV20BillingModelFee> fees = new ArrayList<>();
        YkcV20BillingModelFee fee1 = new YkcV20BillingModelFee();
        fee1.setElectricityRate(BigDecimal.valueOf(1.50000));
        fee1.setServiceRate(BigDecimal.valueOf(0.80000));
        fees.add(fee1);
        YkcV20BillingModelFee fee2 = new YkcV20BillingModelFee();
        fee2.setElectricityRate(BigDecimal.valueOf(0.50000));
        fee2.setServiceRate(BigDecimal.valueOf(0.30000));
        fees.add(fee2);
        resp.setFees(fees);

        resp.setLossRatio(10);
        resp.setTimeSlotRates(Collections.nCopies(48, 2));

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("58");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("58", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20BillingModelSetDown detail = assertInstanceOf(YkcV20BillingModelSetDown.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals("0100", detail.getBillingModelNumber());
        assertEquals(2, detail.getFeeCount());
        assertEquals(10, detail.getLossRatio());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
