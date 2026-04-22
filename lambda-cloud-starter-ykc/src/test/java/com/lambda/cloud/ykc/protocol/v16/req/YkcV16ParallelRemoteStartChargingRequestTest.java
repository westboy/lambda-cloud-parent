package com.lambda.cloud.ykc.protocol.v16.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.req.YkcV16ParallelRemoteStartChargingRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16ParallelRemoteStartChargingRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A4", YkcV16ParallelRemoteStartChargingRequest.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16ParallelRemoteStartChargingRequest req = new YkcV16ParallelRemoteStartChargingRequest();
        req.setTransactionId("55031412782305012018061914444680");
        req.setEquipmentId("55031412782305");
        req.setConnectorId(1);
        req.setLogicalCardNumber("0000001000000573");
        req.setPhysicalCardNumber("00000000d14b0a54");
        req.setAccountBalance(100000L);
        req.setParallelSequence("201029112801");

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A4");
        base.setSerialNumber(31744);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("A4", parsed.getFrameType());
        assertEquals(31744, parsed.getSerialNumber());

        YkcV16ParallelRemoteStartChargingRequest detail =
                assertInstanceOf(YkcV16ParallelRemoteStartChargingRequest.class, parsed.getDetail());
        assertEquals("55031412782305012018061914444680", detail.getTransactionId());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());
        assertEquals("201029112801", detail.getParallelSequence());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
