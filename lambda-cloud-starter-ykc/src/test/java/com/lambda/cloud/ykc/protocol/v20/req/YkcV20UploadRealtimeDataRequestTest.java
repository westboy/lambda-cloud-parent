package com.lambda.cloud.ykc.protocol.v20.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.req.YkcV20UploadRealtimeDataRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20UploadRealtimeDataRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("13", YkcV20UploadRealtimeDataRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20UploadRealtimeDataRequest detail = new YkcV20UploadRealtimeDataRequest();
        detail.setTransactionSerialNumber("1234567890123456789012345678901");
        detail.setEquipmentId("1234567890123");
        detail.setConnectorId("1");
        detail.setStatus(1);
        detail.setGunInPosition(1);
        detail.setGunPlugged(1);
        detail.setOutputVoltage(300);
        detail.setOutputCurrent(300);
        detail.setGunCableTemperature(1);
        detail.setGunCableCode("0123456789abcdef");
        detail.setSoc(1);
        detail.setBatteryMaxTemperature(1);
        detail.setAccumulatedChargingTime(300);
        detail.setRemainingTime(300);
        detail.setChargingEnergy(40000);
        detail.setLossAdjustedChargingEnergy(40000);
        detail.setChargedAmount(40000);
        detail.setHardwareFault(300);
        detail.setPileTemperature(1);
        detail.setSmokeDetectorStatus(1);
        detail.setMeterReading("0123456789");

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("13");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("13", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20UploadRealtimeDataRequest parsedDetail =
                assertInstanceOf(YkcV20UploadRealtimeDataRequest.class, parsed.getDetail());
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
