package com.lambda.cloud.ykc.protocol.billing;

import static org.junit.jupiter.api.Assertions.fail;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import com.lambda.cloud.netty.protocol.encrypt.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.resp.YkcV16BillingModelResponse;
import com.lambda.cloud.ykc.message.v16.resp.YkcV16StartChargingResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 云快充1.7协议计费模型功能测试
 *
 * @author Generated
 */
@Slf4j
public class YkcV16BillingMessageTest {

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
                    "685e00070058111177777777770062a08601008038010068360200204e000098ab0200204e0000e09f0200204e000000000000000000000000000000000000000101010101010101020202020202020202020202030303030303030303030303CE40");

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            ProtocolPayloadRegistry.register("58", YkcV16BillingModelResponse.class);
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
            log.error("计费模型响应序列化测试失败", e);
            fail("计费模型响应序列化失败: " + e.getMessage());
        }
    }

    public void test2() {
        try {
            ProtocolEngine<YkcV16BasePayload> engine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(
                    "682a21BF00321812000000104902239326785321369618120000001049020000000000000000701101000100E818");
            // 将十六进制字符串转换为字节数组
            //            byte[] bytes = HexUtil.decodeHex(
            //
            // "682a164500321812000000104901239610290538086418120000001049010000000000000000701101000100346D");

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            ProtocolPayloadRegistry.register("32", YkcV16StartChargingResponse.class);
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
            log.error("计费模型响应序列化测试失败", e);
            fail("计费模型响应序列化失败: " + e.getMessage());
        }
    }

    private static final String RAW =
            "111177777777770062a08601008038010068360200204e000098ab0200204e0000e09f0200204e000000000000000000000000000000000000000101010101010101020202020202020202020202030303030303030303030303";

    private static final String TEST_DATA4 = "686000070158%s5EB6";

    private static final String TEST_DATA5 =
            "68A2077A003B2037F805A159DE3A41232ECFE8487CF088CF649D2BA64D35CF60F7B6A616FFC52037F805A159DE3A41232ECFE8487CF02E1CBDE1056671A88FF48AA5587144CC21BEF093AB37F25930D97271D43881B0AC0527A95C69C4CB6FE89264E12799EF176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6F568907040EF2BBCE7D4F0AA1ACBEB8B3FBB18110EFC91A40DD03405BFBFED501FA803585DD09612E42FB4B0A609AC26F7FC1B38D486B8141A8560AE76CAB459A56B1C644A56F7E5C1F8FF3B4BB7CAE410736B960D5A3B99E738958AE83488349FF87AA618203A1FE73408F54CD113378587840255A4C0884F21F698B0BCC1D54733";

    @Test
    public void test1() {
        SecretKey key = SecureUtil.generateKey("AES", "0123456789abcdef".getBytes(StandardCharsets.UTF_8));

        byte[] rawBytes = HexUtil.decodeHex(RAW);
        byte[] encryptedBytes = SecureUtil.aes(key.getEncoded()).encrypt(rawBytes);
        String encryptHex = HexUtil.encodeHexStr(encryptedBytes, false);

        System.out.println("rawHex: " + RAW);

        System.out.println("encryptHex: " + encryptHex);

        String format = String.format(TEST_DATA4, encryptHex);
        try {
            EncryptionService encryptionService = new DefaultEncryptionService(key.getEncoded());
            ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(encryptionService);
            ProtocolFieldProcessor protocolFieldProcessor = new ProtocolFieldProcessor(encryptionService);
            reflectionProtocolEngine.setProtocolFieldProcessor(protocolFieldProcessor);
            ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
            ProtocolEngine<YkcV16BasePayload> engine =
                    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

            ProtocolPayloadRegistry.register("58", YkcV16BillingModelResponse.class);

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(format);

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

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
            log.error("计费模型响应序列化测试失败", e);
            fail("计费模型响应序列化失败: " + e.getMessage());
        }
    }
}
