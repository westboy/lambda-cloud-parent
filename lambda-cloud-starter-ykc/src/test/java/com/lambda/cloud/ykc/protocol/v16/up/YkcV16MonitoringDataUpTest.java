package com.lambda.cloud.ykc.protocol.v16.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.up.YkcV16MonitoringDataUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16MonitoringDataUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("13", YkcV16MonitoringDataUp.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16MonitoringDataUp resp = new YkcV16MonitoringDataUp();
        resp.setTransactionId("00000000000000000000000000000000");
        resp.setEquipmentId("55031412782305");
        resp.setConnectorId(1);
        resp.setStatus(0);
        resp.setGunInPlace(1);
        resp.setGunPlugged(1);
        resp.setOutputVoltage(0);
        resp.setOutputCurrent(0);
        resp.setCableTemperature(10);
        resp.setCableCode("0000000000000000");
        resp.setSoc(0);
        resp.setBatteryMaxTemperature(0);
        resp.setTotalChargingTime(0);
        resp.setRemainingTime(0);
        resp.setChargingEnergy(0L);
        resp.setLossAdjustedEnergy(0L);
        resp.setChargedAmount(0L);
        resp.setHardwareFault(0);

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("13");
        base.setSerialNumber(794);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("13", parsed.getFrameType());
        assertEquals(794, parsed.getSerialNumber());
        YkcV16MonitoringDataUp detail = assertInstanceOf(YkcV16MonitoringDataUp.class, parsed.getDetail());
        assertEquals("00000000000000000000000000000000", detail.getTransactionId());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
