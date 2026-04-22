package com.lambda.cloud.ykc.protocol.v20.req;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardSyncCard;
import com.lambda.cloud.ykc.message.v20.req.YkcV20OfflineCardClearRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20OfflineCardClearRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("46", YkcV20OfflineCardClearRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20OfflineCardClearRequest detail = new YkcV20OfflineCardClearRequest();
        detail.setEquipmentId("1234567890123");
        detail.setCardCount(3);
        List<YkcV20OfflineCardSyncCard> cardsList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            YkcV20OfflineCardSyncCard item = new YkcV20OfflineCardSyncCard();
            item.setLogicalCardNo("1234567812345678");
            item.setPhysicalCardNo("0123456789abcdef");
            cardsList.add(item);
        }
        detail.setCards(cardsList);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("46");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("46", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20OfflineCardClearRequest parsedDetail =
                assertInstanceOf(YkcV20OfflineCardClearRequest.class, parsed.getDetail());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals(3, parsedDetail.getCardCount());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
