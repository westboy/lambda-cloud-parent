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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

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

    public static void test0() {
        try {
            ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
            ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
            ProtocolEngine<YkcV16BasePayload> engine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(
                    "683000000023181200000036270125065185339801601812000000362701601B380202DB19630259015F0600F11936023000EF21");

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            ProtocolPayloadRegistry.register("23", YkcV16ElecDetail.class);
            // 使用协议引擎解析消息
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

    public static void main(String[] args) {
        test0();
    }
}
