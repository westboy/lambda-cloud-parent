package com.lambda.cloud.ykc.protocol.v20.resp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.resp.YkcV20StartChargingConfirmResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20StartChargingConfirmResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A6", YkcV20StartChargingConfirmResponse.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20StartChargingConfirmResponse detail = new YkcV20StartChargingConfirmResponse();
        detail.setTransactionSerialNumber("1234567890123456789012345678901");
        detail.setEquipmentId("1234567890123");
        detail.setConnectorId("1");
        detail.setLogicalCardNumber("123456789012345");
        detail.setAccountBalance(40000L);
        detail.setMaxPowerLimit(300);
        detail.setSocLimit(1);
        detail.setEnergyLimit(40000L);
        detail.setAuthenticationSuccess(1);
        detail.setFailureReason("01");

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A6");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("A6", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20StartChargingConfirmResponse parsedDetail =
                assertInstanceOf(YkcV20StartChargingConfirmResponse.class, parsed.getDetail());
        assertEquals("1234567890123456789012345678901", parsedDetail.getTransactionSerialNumber());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals("1", parsedDetail.getConnectorId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
