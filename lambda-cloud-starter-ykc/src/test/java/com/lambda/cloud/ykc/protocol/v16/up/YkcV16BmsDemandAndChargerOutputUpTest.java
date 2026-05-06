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
        resp.setBmsVoltageDemand(0);
        resp.setBmsCurrentDemand(0);
        resp.setBmsChargingMode(0);
        resp.setBmsChargeVoltageMeasured(0);
        resp.setBmsChargeCurrentMeasured(0);
        resp.setBmsVoltageDemand(0);
        resp.setBmsSoc(0);
        resp.setChargerOutputVoltage(0);
        resp.setChargerOutputVoltage(0);
        resp.setBmsEstimatedRemainingTime(0);
        resp.setChargerOutputCurrent(0);
        resp.setBmsHighestCellVoltageAndGroup(0);
        resp.setTotalChargingTime(0);

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
