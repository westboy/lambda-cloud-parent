package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardQueryResult;
import com.lambda.cloud.ykc.message.v20.up.YkcV20OfflineCardQueryReplyUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20OfflineCardQueryReplyUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("47", YkcV20OfflineCardQueryReplyUp.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20OfflineCardQueryReplyUp resp = new YkcV20OfflineCardQueryReplyUp();
        resp.setEquipmentId("15031412782305");

        List<YkcV20OfflineCardQueryResult> results = new ArrayList<>();
        YkcV20OfflineCardQueryResult result1 = new YkcV20OfflineCardQueryResult();
        result1.setPhysicalCardNo("0000000000000001");
        result1.setQueryResult(1);
        results.add(result1);
        resp.setResults(results);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("47");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("47", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20OfflineCardQueryReplyUp detail =
                assertInstanceOf(YkcV20OfflineCardQueryReplyUp.class, parsed.getDetail());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getResults().size());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
