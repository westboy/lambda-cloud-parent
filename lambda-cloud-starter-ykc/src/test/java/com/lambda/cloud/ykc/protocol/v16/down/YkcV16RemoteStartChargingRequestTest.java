package com.lambda.cloud.ykc.protocol.v16.down;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.down.YkcV16RemoteStartChargingDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16RemoteStartChargingRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("34", YkcV16RemoteStartChargingDown.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16RemoteStartChargingDown req = new YkcV16RemoteStartChargingDown();
        req.setTransactionId("55031412782305012018061914444680");
        req.setEquipmentId("55031412782305");
        req.setConnectorId(1);
        req.setLogicalCardNumber("0000001000000573");
        req.setPhysicalCardNumber("00000000d14b0a54");
        req.setAccountBalance(BigDecimal.valueOf(100000L));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("34");
        base.setSerialNumber(31744);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("34", parsed.getFrameType());
        assertEquals(31744, parsed.getSerialNumber());

        YkcV16RemoteStartChargingDown detail =
                assertInstanceOf(YkcV16RemoteStartChargingDown.class, parsed.getDetail());
        assertEquals("55031412782305012018061914444680", detail.getTransactionId());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
