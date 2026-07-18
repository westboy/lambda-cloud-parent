package com.lambda.cloud.t645.netty;

import static org.junit.jupiter.api.Assertions.*;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.encrypt.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.t645.message.T645Frame;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import com.lambda.cloud.t645.message.control.T645BroadcastTime;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatRequest;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatResponse;
import com.lambda.cloud.t645.message.read.T645ReadEnergyResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class T645BodyDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        ReflectionProtocolEngine protocolEngine =
                new ReflectionProtocolEngine(new DefaultEncryptionService("test".getBytes()));

        T645PayloadRegistry.clear();
        T645PayloadRegistry.register(0x00, "NONE", T645HeartbeatRequest.class);
        T645PayloadRegistry.register(0x80, "NONE", T645HeartbeatResponse.class);
        T645PayloadRegistry.register(0x08, T645BroadcastTime.DI, T645BroadcastTime.class);
        T645PayloadRegistry.register(0x91, "00010000", T645ReadEnergyResponse.class);
        T645PayloadRegistry.register(0x91, "00000000", T645ReadEnergyResponse.class);

        channel = new EmbeddedChannel(new T645FrameDecoder(), new T645BodyDecoder(protocolEngine));
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void testHeartbeatWithSignalStrength() throws Exception {
        byte[] bytes = HexUtil.decodeHex("680000000000006800020116E916");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x00, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body, "Body should be parsed");
        assertInstanceOf(T645HeartbeatRequest.class, body);

        T645HeartbeatRequest request = (T645HeartbeatRequest) body;
        assertEquals(0x01, request.getSubFunctionCode());
        assertEquals(0x16, request.getSignalStrength());
    }

    @Test
    void testHeartbeatWithoutSignalStrength() throws Exception {
        byte[] bytes = HexUtil.decodeHex("68000000000000680000D016");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x00, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body, "Body should be parsed even if data is empty");
        assertInstanceOf(T645HeartbeatRequest.class, body);
    }

    @Test
    void testHeartbeatResponse() throws Exception {
        byte[] bytes = HexUtil.decodeHex("680000000000006880005016");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x80, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body);
        assertInstanceOf(T645HeartbeatResponse.class, body);
    }

    @Test
    void testStandardReadEnergyResponseLegacyDi() throws Exception {
        byte[] bytes = HexUtil.decodeHex("6801000000000068910833333333773433334716");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x91, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body);
        assertInstanceOf(T645ReadEnergyResponse.class, body);

        T645ReadEnergyResponse response = (T645ReadEnergyResponse) body;
        assertEquals("00000000", response.getDi());
        assertEquals(new BigDecimal("1.44"), response.getTotalEnergy());
    }

    @Test
    void testBroadcastTimePayload() throws Exception {
        byte[] bytes = HexUtil.decodeHex("680000000000006808063435363738392516");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x08, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body);
        assertInstanceOf(T645BroadcastTime.class, body);

        T645BroadcastTime broadcastTime = (T645BroadcastTime) body;
        assertEquals("010203040506", broadcastTime.getBroadcastTime());
        assertEquals(T645BroadcastTime.DI, broadcastTime.getDi());
    }
}
