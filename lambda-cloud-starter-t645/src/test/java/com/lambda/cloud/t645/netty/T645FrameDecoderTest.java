package com.lambda.cloud.t645.netty;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class T645FrameDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        channel = new EmbeddedChannel(new T645FrameDecoder());
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void testValidFrame() {
        // 68 11 22 33 44 55 66 68 11 01 33 CS 16
        byte[] frameBytes =
                new byte[] {0x68, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x68, 0x11, 0x01, 0x33, (byte) 0x8C, 0x16};
        ByteBuf buf = Unpooled.wrappedBuffer(frameBytes);

        assertTrue(channel.writeInbound(buf));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(13, decoded.readableBytes());

        ReferenceCountUtil.release(decoded);
    }

    @Test
    void testHalfPackage() {
        byte[] frameBytes =
                new byte[] {0x68, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x68, 0x11, 0x01, 0x33, (byte) 0x8C, 0x16};

        ByteBuf buf1 = Unpooled.wrappedBuffer(frameBytes, 0, 5);
        ByteBuf buf2 = Unpooled.wrappedBuffer(frameBytes, 5, 8);

        assertFalse(channel.writeInbound(buf1)); // Not enough data
        assertTrue(channel.writeInbound(buf2)); // Full frame received

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(13, decoded.readableBytes());

        ReferenceCountUtil.release(decoded);
    }

    @Test
    void testStickyPackageAndGarbage() {
        byte[] frameBytes = new byte[] {
            0x00,
            0x01, // Garbage
            0x68,
            0x11,
            0x22,
            0x33,
            0x44,
            0x55,
            0x66,
            0x68,
            0x11,
            0x01,
            0x33,
            (byte) 0x8C,
            0x16,
            0x02, // Garbage
            0x68,
            0x11,
            0x22,
            0x33,
            0x44,
            0x55,
            0x66,
            0x68,
            0x11,
            0x00,
            (byte) 0x59,
            0x16
        };
        ByteBuf buf = Unpooled.wrappedBuffer(frameBytes);

        assertTrue(channel.writeInbound(buf));

        ByteBuf decoded1 = channel.readInbound();
        assertNotNull(decoded1);
        assertEquals(13, decoded1.readableBytes());
        ReferenceCountUtil.release(decoded1);

        ByteBuf decoded2 = channel.readInbound();
        assertNotNull(decoded2);
        assertEquals(12, decoded2.readableBytes());
        ReferenceCountUtil.release(decoded2);
    }
}
