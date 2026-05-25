package com.lambda.cloud.t645.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class T645CodecSupportTest {

    @Test
    void testSubOffset33() {
        byte[] input = new byte[] {(byte) 0x66, (byte) 0x88};
        byte[] result = T645CodecSupport.subOffset33(input);
        assertArrayEquals(new byte[] {0x33, 0x55}, result);

        // 测试空值
        assertArrayEquals(new byte[0], T645CodecSupport.subOffset33(null));

        // 测试边界（借位）
        byte[] input2 = new byte[] {0x11};
        byte[] result2 = T645CodecSupport.subOffset33(input2);
        assertEquals((byte) 0xDE, result2[0]); // 0x11 - 0x33 = -0x22 (0xDE in byte)
    }

    @Test
    void testAddOffset33() {
        byte[] input = new byte[] {0x33, 0x55};
        byte[] result = T645CodecSupport.addOffset33(input);
        assertArrayEquals(new byte[] {(byte) 0x66, (byte) 0x88}, result);

        // 测试空值
        assertArrayEquals(new byte[0], T645CodecSupport.addOffset33(null));

        // 测试边界（进位）
        byte[] input2 = new byte[] {(byte) 0xEE};
        byte[] result2 = T645CodecSupport.addOffset33(input2);
        assertEquals(0x21, result2[0]); // 0xEE + 0x33 = 0x121 -> 0x21
    }

    @Test
    void testReverseAddress() {
        assertEquals("665544332211", T645CodecSupport.reverseAddress("112233445566"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            T645CodecSupport.reverseAddress("123");
        });
        assertTrue(exception.getMessage().contains("地址域长度必须为12个十六进制字符"));

        assertThrows(IllegalArgumentException.class, () -> {
            T645CodecSupport.reverseAddress(null);
        });
    }

    @Test
    void testCalculateSum8() {
        // 68 11 22 33 44 55 66 68 11 01 33
        byte[] data = new byte[] {0x68, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x68, 0x11, 0x01, 0x33};
        int sum = T645CodecSupport.calculateSum8(data);
        assertEquals(0x7A, sum); // 0x27A & 0xFF = 0x7A

        assertEquals(0, T645CodecSupport.calculateSum8(null));
    }

    @Test
    void testBuildDiKey() {
        assertEquals("11:00010000", T645CodecSupport.buildDiKey(0x11, "00010000"));
        assertEquals("91:C0320000", T645CodecSupport.buildDiKey(0x91, "c0320000")); // 测试大小写
        assertEquals("00:", T645CodecSupport.buildDiKey(0x00, null)); // 测试空值
    }
}
