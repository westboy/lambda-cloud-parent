package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.down.YkcV20OfflineCardClearResponse;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardClearResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20OfflineCardClearResponseTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("45", YkcV20OfflineCardClearResponse.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20OfflineCardClearResponse detail = new YkcV20OfflineCardClearResponse();
        detail.setEquipmentId("1234567890123");
        List<YkcV20OfflineCardClearResult> results = new ArrayList<>();
        YkcV20OfflineCardClearResult item = new YkcV20OfflineCardClearResult();
        item.setPhysicalCardNo("0123456789abcdef");
        item.setClearFlag(1);
        item.setFailReason(0);
        results.add(item);
        detail.setResults(results);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("45");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("45", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20OfflineCardClearResponse parsedDetail =
                assertInstanceOf(YkcV20OfflineCardClearResponse.class, parsed.getDetail());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
