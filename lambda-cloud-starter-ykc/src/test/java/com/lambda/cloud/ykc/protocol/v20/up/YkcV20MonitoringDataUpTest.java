package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.up.YkcV20MonitoringDataUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20MonitoringDataUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("13", YkcV20MonitoringDataUp.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20MonitoringDataUp resp = new YkcV20MonitoringDataUp();
        resp.setTransactionSerialNumber("00000000000000000000000000000000");
        resp.setEquipmentId("15031412782305");
        resp.setConnectorId("01");
        resp.setStatus(2);
        resp.setGunInPosition(1);
        resp.setGunPlugged(0);
        resp.setOutputVoltage(0);
        resp.setOutputCurrent(0);
        resp.setGunCableTemperature(50);
        resp.setGunCableCode("0000000000000000");
        resp.setSoc(0);
        resp.setBatteryMaxTemperature(0);
        resp.setAccumulatedChargingTime(0);
        resp.setRemainingTime(0);
        resp.setChargingEnergy(0);
        resp.setLossAdjustedChargingEnergy(0);
        resp.setChargedAmount(0);
        resp.setHardwareFault(0);
        resp.setPileTemperature(50);
        resp.setSmokeDetectorStatus(2);
        resp.setMeterReading("0000000000");

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("13");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("13", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20MonitoringDataUp detail = assertInstanceOf(YkcV20MonitoringDataUp.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals("01", detail.getConnectorId());
        assertEquals(2, detail.getStatus());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
