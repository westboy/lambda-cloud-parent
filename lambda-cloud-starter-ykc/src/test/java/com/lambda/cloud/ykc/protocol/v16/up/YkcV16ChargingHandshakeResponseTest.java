package com.lambda.cloud.ykc.protocol.v16.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.up.YkcV16ChargingHandshakeUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16ChargingHandshakeResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("15", YkcV16ChargingHandshakeUp.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16ChargingHandshakeUp resp = new YkcV16ChargingHandshakeUp();
        resp.setTransactionId("32010200000000111511161555350260");
        resp.setEquipmentId("32010200000001");
        resp.setConnectorId(1);
        resp.setBmsProtocolVersion("000000");
        resp.setBatteryType(0);
        resp.setRatedCapacity(0);
        resp.setRatedTotalVoltage(0);
        resp.setManufacturerName("");
        resp.setBatterySequence("00000000");
        resp.setProductionYear(0);
        resp.setProductionMonth(0);
        resp.setProductionDay(0);
        resp.setChargeCycleCount(0);
        resp.setBatteryOwner(0);
        resp.setReserved(String.valueOf(0));
        resp.setVinCode("");
        resp.setBmsSoftwareVersion("0000000000000000");

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("15");
        base.setSerialNumber(21);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("15", parsed.getFrameType());
        assertEquals(21, parsed.getSerialNumber());

        YkcV16ChargingHandshakeUp detail = assertInstanceOf(YkcV16ChargingHandshakeUp.class, parsed.getDetail());
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
