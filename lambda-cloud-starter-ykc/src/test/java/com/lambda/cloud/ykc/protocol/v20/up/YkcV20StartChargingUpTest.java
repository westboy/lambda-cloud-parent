package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.up.YkcV20StartChargingUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20StartChargingUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A5", YkcV20StartChargingUp.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);
        String vin = "LJ12ABC34DEF56789";
        String reversedVin = new StringBuilder(vin).reverse().toString();

        YkcV20StartChargingUp req = new YkcV20StartChargingUp();
        req.setEquipmentId("15031412782305");
        req.setConnectorId("01");
        req.setStartMethod(3);
        req.setNeedPassword(0);
        req.setAccountOrCardNumber("0000000000000000");
        req.setInputPassword("0000000000000000");
        req.setVin(vin);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A5");
        base.setSerialNumber(1);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("A5", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20StartChargingUp detail = assertInstanceOf(YkcV20StartChargingUp.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals("01", detail.getConnectorId());
        assertEquals(3, detail.getStartMethod());
        assertEquals(0, detail.getNeedPassword());
        assertEquals(vin, detail.getVin());
        assertTrue(new String(raw, StandardCharsets.ISO_8859_1).contains(reversedVin));

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
