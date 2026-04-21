package com.lambda.cloud.ykc.protocol.v16.resp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.resp.YkcV16ParallelStartChargingResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16ParallelStartChargingResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("A2", YkcV16ParallelStartChargingResponse.class);
        ProtocolEngine<YkcV16BasePayload> engine = ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16ParallelStartChargingResponse resp = new YkcV16ParallelStartChargingResponse();
        resp.setTransactionId("32010200000001012018061219595785");
        resp.setEquipmentId("32010200000001");
        resp.setConnectorId(1);
        resp.setLogicalCardNumber("0000000000000000");
        resp.setAccountBalance(0L);
        resp.setAuthResult(0);
        resp.setFailureReason(1);
        resp.setParallelSequence("201029112801");

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("A2");
        base.setSerialNumber(1024);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("A2", parsed.getFrameType());
        assertEquals(1024, parsed.getSerialNumber());

        YkcV16ParallelStartChargingResponse detail =
                assertInstanceOf(YkcV16ParallelStartChargingResponse.class, parsed.getDetail());
        assertEquals("32010200000001012018061219595785", detail.getTransactionId());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());
        assertEquals("201029112801", detail.getParallelSequence());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
