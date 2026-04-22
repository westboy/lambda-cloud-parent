package com.lambda.cloud.ykc.protocol.v20.resp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import com.lambda.cloud.ykc.message.v20.resp.YkcV20BillingModelResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20BillingModelResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("0A", YkcV20BillingModelResponse.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20BillingModelResponse detail = new YkcV20BillingModelResponse();
        detail.setEquipmentId("1234567890123");
        detail.setBillingModelNumber("123");
        detail.setRateCount(1);
        detail.setLossRatio(1);
        detail.setRateCount(3);
        List<YkcV20BillingModelFee> feesList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            YkcV20BillingModelFee item = new YkcV20BillingModelFee();
            item.setElectricityRate(new BigDecimal("2"));
            item.setServiceRate(new BigDecimal("1"));
            feesList.add(item);
        }
        detail.setFees(feesList);
        detail.setTimeSlotRateNumbers(java.util.Collections.nCopies(48, 0));

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("0A");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("0A", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20BillingModelResponse parsedDetail =
                assertInstanceOf(YkcV20BillingModelResponse.class, parsed.getDetail());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals("123", parsedDetail.getBillingModelNumber());
        assertEquals(1, parsedDetail.getRateCount());
        assertEquals(48, parsedDetail.getTimeSlotRateNumbers().size());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
