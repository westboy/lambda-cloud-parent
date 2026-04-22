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

public class CompositeListMessageTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void parse_shouldHandleCompositeListWithSizeField() throws Exception {
        ProtocolEngine<CompositeListMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] raw = HexUtil.decodeHex("0201020304");
        ByteBuf in = Unpooled.wrappedBuffer(raw);

        CompositeListMessage msg = engine.parse(in, CompositeListMessage.class);
        assertEquals(2, msg.getCount());
        assertEquals(2, msg.getItems().size());
        assertEquals(1, msg.getItems().get(0).getA());
        assertEquals(2, msg.getItems().get(0).getB());
        assertEquals(3, msg.getItems().get(1).getA());
        assertEquals(4, msg.getItems().get(1).getB());

        ByteBuf out = Unpooled.buffer();
        engine.serialize(msg, out);
        byte[] serialized = new byte[out.readableBytes()];
        out.readBytes(serialized);
        assertArrayEquals(raw, serialized);
    }
}
