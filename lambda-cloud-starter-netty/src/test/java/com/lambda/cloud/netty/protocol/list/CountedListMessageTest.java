package com.lambda.cloud.netty.protocol.list;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CountedListMessageTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void parse_shouldRespectListElementSizeField() throws Exception {
        ProtocolEngine<CountedListMessage> engine = ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] raw = HexUtil.decodeHex("03010203");
        ByteBuf in = Unpooled.wrappedBuffer(raw);

        CountedListMessage msg = engine.parse(in, CountedListMessage.class);
        assertEquals(3, msg.getCount());
        assertEquals(3, msg.getValues().size());
        assertEquals(1, msg.getValues().get(0));
        assertEquals(2, msg.getValues().get(1));
        assertEquals(3, msg.getValues().get(2));

        ByteBuf out = Unpooled.buffer();
        engine.serialize(msg, out);
        byte[] serialized = new byte[out.readableBytes()];
        out.readBytes(serialized);
        assertArrayEquals(raw, serialized);
    }
}

