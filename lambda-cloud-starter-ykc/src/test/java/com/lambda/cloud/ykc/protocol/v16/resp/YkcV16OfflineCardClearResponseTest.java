package com.lambda.cloud.ykc.protocol.v16.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.model.YkcV16OfflineCardClearResult;
import com.lambda.cloud.ykc.message.v16.resp.YkcV16OfflineCardClearResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16OfflineCardClearResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldKeepAllItems() throws Exception {
        ProtocolPayloadRegistry.register("45", YkcV16OfflineCardClearResponse.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16OfflineCardClearResult r1 = new YkcV16OfflineCardClearResult();
        r1.setPhysicalCardNo("00000000d14b0a54");
        r1.setClearFlag(0);
        r1.setFailReason(1);
        YkcV16OfflineCardClearResult r2 = new YkcV16OfflineCardClearResult();
        r2.setPhysicalCardNo("00000000e14c0a54");
        r2.setClearFlag(1);
        r2.setFailReason(0);

        YkcV16OfflineCardClearResponse resp = new YkcV16OfflineCardClearResponse();
        resp.setEquipmentId("32010200000001");
        resp.setResults(List.of(r1, r2));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("45");
        base.setSerialNumber(7);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("45", parsed.getFrameType());
        assertEquals(7, parsed.getSerialNumber());

        YkcV16OfflineCardClearResponse detail =
                assertInstanceOf(YkcV16OfflineCardClearResponse.class, parsed.getDetail());
        assertEquals(2, detail.getResults().size());
        assertEquals("00000000d14b0a54", detail.getResults().get(0).getPhysicalCardNo());
        assertEquals("00000000e14c0a54", detail.getResults().get(1).getPhysicalCardNo());
    }
}
