package com.lambda.cloud.ykc.protocol.login;

import static org.junit.jupiter.api.Assertions.*;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.ykc.message.v17.req.YkcV17LoginRequest;
import com.lambda.cloud.ykc.message.v17.req.YkcV17LoginRequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 云快充1.7协议登录功能测试
 * <p>
 * 测试登录请求和登录响应消息的序列化和反序列化
 * </p>
 *
 * @author Generated
 */
@Slf4j
public class YkcV17LoginMessageTest {

    @BeforeEach
    public void setUp() {
        // 初始化协议引擎
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
    }

    /**
     * 测试登录请求消息的创建和序列化
     */
    @Test
    public void testLoginRequestSerialization1() {
        log.info("开始测试登录请求消息的序列化");

        try {
            ProtocolEngine<YkcV17LoginRequestPayload> engine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            // 将十六进制字符串转换为字节数组
            byte[] bytes =
                    HexUtil.decodeHex("6822E80300010181200000000500021056312E342E312E35010000000000000000000104FC40");

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

            // 使用协议引擎解析消息
            YkcV17LoginRequestPayload record = engine.parse(byteBuf, YkcV17LoginRequestPayload.class);

            System.out.println(record);

            ByteBuf serializeBuffer = Unpooled.buffer();
            engine.serialize(record, serializeBuffer);

            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);
            String hexString = HexUtil.encodeHexStr(serializedBytes, false);
            System.out.println(hexString);
        } catch (Exception e) {
            log.error("登录请求序列化测试失败", e);
            fail("登录请求序列化失败: " + e.getMessage());
        }
    }

    /**
     * 测试登录请求消息的创建和序列化
     */
    @Test
    public void testLoginRequestSerialization() {
        log.info("开始测试登录请求消息的序列化");

        try {
            ProtocolEngine<YkcV17LoginRequestPayload> loginRequestEngine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            // 创建登录请求消息体
            YkcV17LoginRequest requestDetail = new YkcV17LoginRequest();
            requestDetail.setEquipmentId("1812000000005");
            requestDetail.setEquipmentType(0);
            requestDetail.setConnectorCount(2);
            requestDetail.setProtocolVersion(10);
            requestDetail.setProgramVersion("V1.4.1.50");
            requestDetail.setNetworkType(1);
            requestDetail.setSimCardNumber("1");
            requestDetail.setOperator(4);

            // 创建登录请求消息
            YkcV17LoginRequestPayload requestMessage = new YkcV17LoginRequestPayload(requestDetail);
            requestMessage.setStartFlag("68");
            requestMessage.setSerialNumber(1000);
            requestMessage.setEncryptFlag("00");

            // 序列化消息
            ByteBuf serializeBuffer = Unpooled.buffer();
            loginRequestEngine.serialize(requestMessage, serializeBuffer);

            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);
            String hexString = HexUtil.encodeHexStr(serializedBytes, false);

            log.info("登录请求序列化结果: {}", hexString);
            log.info("序列化数据长度: {} 字节", serializedBytes.length);

            // 验证序列化结果不为空
            assertNotNull(serializedBytes);
            assertTrue(serializedBytes.length > 0);

            // 验证起始符
            assertEquals("68", hexString.substring(0, 2).toUpperCase());

            log.info("登录请求序列化测试通过");

        } catch (Exception e) {
            log.error("登录请求序列化测试失败", e);
            fail("登录请求序列化失败: " + e.getMessage());
        }
    }
}
