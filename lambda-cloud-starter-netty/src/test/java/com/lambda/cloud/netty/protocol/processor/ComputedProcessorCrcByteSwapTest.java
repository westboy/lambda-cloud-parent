package com.lambda.cloud.netty.protocol.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolPayloadMetadata;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.checksum.ChecksumFactory;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterResolver;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CRC 字节序兼容开关测试
 * <p>
 * 验证开启 spring.netty.protocol.crc-byte-swap 后，CRC 校验失败时会按
 * CRC 字段长度对计算值做字节交换后再比对
 * </p>
 *
 * @author Jin
 */
public class ComputedProcessorCrcByteSwapTest {

    /**
     * 参与 CRC 计算的测试数据
     */
    private static final byte[] PAYLOAD = {0x12, 0x34};

    private static final String CRC_ALGORITHM = "CRC16-MODBUS";

    private ProtocolPayloadMetadata metadata;

    private ComputedProcessor computedProcessor;

    @Getter
    @Setter
    @ProtocolPayload(frameType = "crcSwapTest", name = "CRC字节序兼容测试", isFrame = true)
    public static class CrcSwapTestMessage {

        @ProtocolField(order = 0, length = 2, dataType = ProtocolDataType.HEX, computed = true, description = "测试数据")
        private String data;

        @ProtocolField(order = 1, length = 2, dataType = ProtocolDataType.HEX, crcFiled = true, description = "校验码")
        private String checksum;
    }

    @BeforeEach
    void setUp() {
        computedProcessor = new ComputedProcessor(new DataTypeConverterResolver());
        metadata = new ReflectionProtocolEngine(null).getMetadata(CrcSwapTestMessage.class);
        ComputedProcessor.setCrcByteSwap(false);
    }

    @AfterEach
    void tearDown() {
        ComputedProcessor.setCrcByteSwap(false);
    }

    @Test
    public void testDirectMatchPassesWhenSwitchOff() {
        long crc = calculateCrc();
        CrcSwapTestMessage message = messageWithChecksum(String.format("%04X", crc));

        assertDoesNotThrow(() -> computedProcessor.validateCrc(message, Unpooled.wrappedBuffer(PAYLOAD), metadata));
    }

    @Test
    public void testSwappedCrcFailsWhenSwitchOff() {
        CrcSwapTestMessage message = messageWithChecksum(swapHex(calculateCrc()));

        assertThrows(
                ProtocolException.class,
                () -> computedProcessor.validateCrc(message, Unpooled.wrappedBuffer(PAYLOAD), metadata));
    }

    @Test
    public void testSwappedCrcPassesWhenSwitchOn() {
        ComputedProcessor.setCrcByteSwap(true);
        CrcSwapTestMessage message = messageWithChecksum(swapHex(calculateCrc()));

        assertDoesNotThrow(() -> computedProcessor.validateCrc(message, Unpooled.wrappedBuffer(PAYLOAD), metadata));
    }

    @Test
    public void testDirectMatchStillPassesWhenSwitchOn() {
        ComputedProcessor.setCrcByteSwap(true);
        long crc = calculateCrc();
        CrcSwapTestMessage message = messageWithChecksum(String.format("%04X", crc));

        assertDoesNotThrow(() -> computedProcessor.validateCrc(message, Unpooled.wrappedBuffer(PAYLOAD), metadata));
    }

    @Test
    public void testWrongCrcStillFailsWhenSwitchOn() {
        ComputedProcessor.setCrcByteSwap(true);
        CrcSwapTestMessage message = messageWithChecksum("FFFF");

        ProtocolException exception = assertThrows(
                ProtocolException.class,
                () -> computedProcessor.validateCrc(message, Unpooled.wrappedBuffer(PAYLOAD), metadata));
        assertEquals(ProtocolException.ErrorCode.CRC_VALIDATION_ERROR, exception.getErrorCode());
    }

    private long calculateCrc() {
        return ChecksumFactory.getAlgorithm(CRC_ALGORITHM).calculate(PAYLOAD);
    }

    /**
     * 模拟设备按小端序存储 CRC、但协议字段按大端解析后得到的值
     */
    private String swapHex(long crc) {
        long swapped = ((crc & 0xFF) << 8) | ((crc >>> 8) & 0xFF);
        return String.format("%04X", swapped);
    }

    private CrcSwapTestMessage messageWithChecksum(String hexChecksum) {
        CrcSwapTestMessage message = new CrcSwapTestMessage();
        message.setData("1234");
        message.setChecksum(hexChecksum);
        return message;
    }
}
