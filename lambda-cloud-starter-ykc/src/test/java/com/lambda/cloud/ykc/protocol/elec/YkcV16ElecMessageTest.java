package com.lambda.cloud.ykc.protocol.elec;

import static org.junit.jupiter.api.Assertions.fail;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 云快充1.7协议计费模型功能测试
 *
 * @author Generated
 */
@Slf4j
public class YkcV16ElecMessageTest {

    @BeforeEach
    public void setUp() {
        // 初始化协议引擎
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
    }

    @Test
    public void test0() {
        try {
            ProtocolEngine<YkcV16BasePayload> engine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(
                    "6830154900231812000000295301250653618950963218120000002953014010B20C02610FC50C05005B0900640FBA0C16000F89");

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            ProtocolPayloadRegistry.register("23", YkcV16ElecDetail.class);
            // 使用协议引擎解析消息，利用泛型消除强制类型转换
            YkcV16BasePayload record = engine.parse(byteBuf, YkcV16BasePayload.class);
            System.out.println(record);

            ByteBuf serializeBuffer = Unpooled.buffer();
            engine.serialize(record, serializeBuffer);

            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);
            String hexString = HexUtil.encodeHexStr(serializedBytes, false);
            System.out.println(hexString);
        } catch (Exception e) {
            fail("响应序列化失败: " + e.getMessage());
        }
    }

    /**
     * 解析电流值（针对 2 字节 BIN 类型）
     * @param hexValue 16进制字符串，如 "3802"
     * @return 实际电流 A
     */
    public static double parseCurrent(String hexValue) {
        // 1. 16进制转10进制整型
        int rawInt = Integer.parseInt(hexValue, 2);

        // 2. 根据协议公式计算：raw * 0.1 - 400
        // 建议使用 BigDecimal 避免浮点数精度问题
        return BigDecimal.valueOf(rawInt)
                .multiply(new BigDecimal("0.1"))
                .subtract(new BigDecimal("400"))
                .doubleValue();
    }
}
