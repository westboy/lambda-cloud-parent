package com.lambda.cloud.t645.netty;

import static org.junit.jupiter.api.Assertions.*;

import com.lambda.cloud.netty.protocol.encrypt.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.t645.message.T645Frame;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatRequest;
import com.lambda.cloud.t645.message.heartbeat.T645HeartbeatResponse;
import com.lambda.cloud.t645.message.read.T645ReadEnergyResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.math.BigDecimal;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class T645BodyDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        ReflectionProtocolEngine protocolEngine =
                new ReflectionProtocolEngine(new DefaultEncryptionService("test".getBytes()));

        // 注册业务模型
        T645PayloadRegistry.clear();
        T645PayloadRegistry.register(0x00, "NONE", T645HeartbeatRequest.class);
        T645PayloadRegistry.register(0x80, "NONE", T645HeartbeatResponse.class);
        T645PayloadRegistry.register(0x91, "00000000", T645ReadEnergyResponse.class);

        // 构建解码链路：FrameDecoder(切包) -> T645BodyDecoder(外壳解析+业务分发)
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
        // 携带信号强度的心跳：68 00 00 00 00 00 00 68 00 02 01 16 E9 16
        byte[] bytes = Hex.decodeHex("680000000000006800020116E916");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x00, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body, "Body should be parsed");
        assertTrue(body instanceof T645HeartbeatRequest);

        T645HeartbeatRequest request = (T645HeartbeatRequest) body;
        assertEquals(0x01, request.getSubFunctionCode());
        assertEquals(0x16, request.getSignalStrength());
    }

    @Test
    void testHeartbeatWithoutSignalStrength() throws Exception {
        // 无信号强度心跳：68 00 00 00 00 00 00 68 00 00 D0 16
        byte[] bytes = Hex.decodeHex("68000000000000680000D016");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x00, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body, "Body should be parsed even if data is empty");
        assertTrue(body instanceof T645HeartbeatRequest);
    }

    @Test
    void testHeartbeatResponse() throws Exception {
        // 平台回复心跳帧应答：68 00 00 00 00 00 00 68 80 00 50 16
        byte[] bytes = Hex.decodeHex("680000000000006880005016");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x80, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body);
        assertTrue(body instanceof T645HeartbeatResponse);
    }

    @Test
    void testStandardReadEnergyResponse() throws Exception {
        // 有功总电能回复：68 01 00 00 00 00 00 68 91 08 33 33 33 33 77 34 33 33 47 16 (原文档笔误为4A)
        byte[] bytes = Hex.decodeHex("6801000000000068910833333333773433334716");
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        assertTrue(channel.writeInbound(buf));

        T645Frame frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(0x91, frame.getControlCode());

        Object body = frame.getBody();
        assertNotNull(body);
        assertTrue(body instanceof T645ReadEnergyResponse);

        T645ReadEnergyResponse response = (T645ReadEnergyResponse) body;
        assertEquals("00000000", response.getDi());

        // 原始数据: 77 34 33 33
        // 减去33H: 44 01 00 00
        // BCD倒序: 00 00 01 44
        // 两位小数: 1.44
        assertEquals(new BigDecimal("1.44"), response.getTotalEnergy());
    }
}
