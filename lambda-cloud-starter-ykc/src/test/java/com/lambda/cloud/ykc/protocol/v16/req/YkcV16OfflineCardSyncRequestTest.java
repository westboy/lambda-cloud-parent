package com.lambda.cloud.ykc.protocol.v16.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.model.YkcV16OfflineCardSyncCard;
import com.lambda.cloud.ykc.message.v16.req.YkcV16OfflineCardSyncRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16OfflineCardSyncRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldKeepAllItems() throws Exception {
        ProtocolPayloadRegistry.register("44", YkcV16OfflineCardSyncRequest.class);
        ProtocolEngine<YkcV16BasePayload> engine = ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV16OfflineCardSyncCard card = new YkcV16OfflineCardSyncCard();
        card.setLogicalCardNo("0000000010000001");
        card.setPhysicalCardNo("00000000d14b0a54");

        YkcV16OfflineCardSyncRequest req = new YkcV16OfflineCardSyncRequest();
        req.setEquipmentId("32010200000001");
        req.setCardCount(1);
        req.setCards(List.of(card));

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("44");
        base.setSerialNumber(7);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("44", parsed.getFrameType());
        assertEquals(7, parsed.getSerialNumber());

        YkcV16OfflineCardSyncRequest detail = assertInstanceOf(YkcV16OfflineCardSyncRequest.class, parsed.getDetail());
        assertEquals("32010200000001", detail.getEquipmentId());
        assertEquals(1, detail.getCardCount());
        assertEquals(1, detail.getCards().size());
        assertEquals("0000000010000001", detail.getCards().get(0).getLogicalCardNo());
        assertEquals("00000000d14b0a54", detail.getCards().get(0).getPhysicalCardNo());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
