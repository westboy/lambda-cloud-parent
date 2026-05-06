package com.lambda.cloud.ykc.protocol.v16.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.down.YkcV16AccountBalanceUpdateDown;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16AccountBalanceUpdateRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("42", YkcV16AccountBalanceUpdateDown.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16AccountBalanceUpdateDown req = new YkcV16AccountBalanceUpdateDown();
        req.setEquipmentId("32010200000001");
        req.setConnectorId(1);
        req.setPhysicalCardNumber("00000000d14b0a54");
        req.setUpdatedAccountBalance(new BigDecimal("0.00"));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("42");
        base.setSerialNumber(1536);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);

        assertEquals("42", parsed.getFrameType());
        assertEquals(1536, parsed.getSerialNumber());

        YkcV16AccountBalanceUpdateDown detail =
                assertInstanceOf(YkcV16AccountBalanceUpdateDown.class, parsed.getDetail());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());
        assertEquals("00000000d14b0a54", detail.getPhysicalCardNumber());
        assertEquals(new BigDecimal("0.00"), detail.getUpdatedAccountBalance());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
