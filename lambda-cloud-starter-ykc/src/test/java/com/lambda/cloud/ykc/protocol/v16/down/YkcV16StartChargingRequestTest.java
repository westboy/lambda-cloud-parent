package com.lambda.cloud.ykc.protocol.v16.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.up.YkcV16StartChargingUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16StartChargingRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("31", YkcV16StartChargingUp.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16StartChargingUp req = new YkcV16StartChargingUp();
        req.setEquipmentId("32010200000001");
        req.setConnectorId(1);
        req.setStartMethod(1);
        req.setPasswordRequired(0);
        req.setAccountOrCardNumber("00000000d14b0a54");
        req.setPassword("00000000000000000000000000000000");
        req.setVinCode("");

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("31");
        base.setSerialNumber(1024);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("31", parsed.getFrameType());
        assertEquals(1024, parsed.getSerialNumber());

        YkcV16StartChargingUp detail = assertInstanceOf(YkcV16StartChargingUp.class, parsed.getDetail());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());
        assertEquals(1, detail.getStartMethod());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
        String hexString = HexUtil.encodeHexStr(raw, false);
        System.out.println(hexString);
    }
}
