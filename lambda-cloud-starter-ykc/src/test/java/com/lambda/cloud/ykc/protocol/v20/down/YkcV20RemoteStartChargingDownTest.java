package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.down.YkcV20RemoteStartChargingDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20RemoteStartChargingDownTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A8", YkcV20RemoteStartChargingDown.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20RemoteStartChargingDown resp = new YkcV20RemoteStartChargingDown();
        resp.setTransactionSerialNumber("55031412782305012018061910262392");
        resp.setEquipmentId("15031412782305");
        resp.setConnectorId("01");
        resp.setLogicalCardNumber("0000000000000000");
        resp.setPhysicalCardNumber("0000000000000000");
        resp.setAccountBalance(10000);
        resp.setMaxPower(120);
        resp.setSocLimit(0);
        resp.setChargingEnergyLimit(0);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A8");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("A8", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20RemoteStartChargingDown detail =
                assertInstanceOf(YkcV20RemoteStartChargingDown.class, parsed.getDetail());
        assertEquals("55031412782305012018061910262392", detail.getTransactionSerialNumber());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals("01", detail.getConnectorId());
        assertEquals(120, detail.getMaxPower());
        assertEquals(0, detail.getSocLimit());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
