package com.lambda.cloud.ykc.protocol.v20.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.req.YkcV20ChargingHandshakeRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20ChargingHandshakeRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("15", YkcV20ChargingHandshakeRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine = ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20ChargingHandshakeRequest detail = new YkcV20ChargingHandshakeRequest();
        detail.setTransactionSerialNumber("1234567890123456789012345678901");
        detail.setEquipmentId("1234567890123");
        detail.setConnectorId("1");
        detail.setBmsProtocolVersion("012345");
        detail.setBmsBatteryType(1);
        detail.setBmsRatedCapacity(300);
        detail.setBmsRatedVoltage(300);
        detail.setBmsManufacturerName("A".repeat(4));
        detail.setBmsBatteryPackSerialNumber("01234567");
        detail.setBmsProductionYear(1);
        detail.setBmsProductionMonth(1);
        detail.setBmsProductionDay(1);
        detail.setBmsChargingCycles("012345");
        detail.setBmsOwnershipIdentifier(1);
        detail.setReserved1(1);
        detail.setBmsVinCode("A".repeat(17));
        detail.setBmsSoftwareVersion("0123456789abcdef");

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("15");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("15", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20ChargingHandshakeRequest parsedDetail = assertInstanceOf(YkcV20ChargingHandshakeRequest.class, parsed.getDetail());
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
