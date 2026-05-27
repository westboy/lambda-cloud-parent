package com.lambda.cloud.ykc.protocol.v16.up;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.up.YkcV16BmsDemandAndChargerOutputUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16BmsDemandAndChargerOutputUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("23", YkcV16BmsDemandAndChargerOutputUp.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16BmsDemandAndChargerOutputUp resp = new YkcV16BmsDemandAndChargerOutputUp();
        resp.setTransactionId("32010200000000111511161555350260");
        resp.setEquipmentId("32010200000001");
        resp.setConnectorId(1);
        resp.setBmsVoltageDemand(3600);
        resp.setBmsCurrentDemand(1500);
        resp.setBmsChargingMode(1);
        resp.setBmsChargeVoltageMeasured(3500);
        resp.setBmsChargeCurrentMeasured(1200);
        resp.setBmsHighestCellVoltageAndGroup(0x03E8);
        resp.setBmsSoc(80);
        resp.setBmsEstimatedRemainingTime(120);
        resp.setChargerOutputVoltage(3550);
        resp.setChargerOutputCurrent(1300);
        resp.setTotalChargingTime(60);

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("23");
        base.setSerialNumber(25);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("23", parsed.getFrameType());
        assertEquals(25, parsed.getSerialNumber());

        YkcV16BmsDemandAndChargerOutputUp detail =
                assertInstanceOf(YkcV16BmsDemandAndChargerOutputUp.class, parsed.getDetail());
        assertEquals("32010200000000111511161555350260", detail.getTransactionId());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
