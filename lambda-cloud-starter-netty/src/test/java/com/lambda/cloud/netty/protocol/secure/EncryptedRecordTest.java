package com.lambda.cloud.netty.protocol.secure;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.checksum.ChecksumService;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import com.lambda.cloud.netty.protocol.encrypt.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 交易记录协议解析测试
 * <p>
 * 使用新的协议框架解析testData3报文
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class EncryptedRecordTest {

    private static final String RAW =
            "181200000002660116430696294154241812000000026601204E0E0C120A19F0D21C0D120A19000000006879370000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009AA9534F0002238B4F006879370000000000000000004C4741473450593333533630313332333501F0D21C0D120A19410150BE785B3E781B";
    private static final String TEST_DATA4 =
            "68A2077A003B181200000002660116430696294154241812000000026601204E0E0C120A19F0D21C0D120A19000000006879370000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009AA9534F0002238B4F006879370000000000000000004C4741473450593333533630313332333501F0D21C0D120A19410150BE785B3E781B4733";

    private static final String TEST_DATA5 =
            "68A2077A003B2037F805A159DE3A41232ECFE8487CF088CF649D2BA64D35CF60F7B6A616FFC52037F805A159DE3A41232ECFE8487CF02E1CBDE1056671A88FF48AA5587144CC21BEF093AB37F25930D97271D43881B0AC0527A95C69C4CB6FE89264E12799EF176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6176C295A595D7193D095F1CE05665AB6F568907040EF2BBCE7D4F0AA1ACBEB8B3FBB18110EFC91A40DD03405BFBFED501FA803585DD09612E42FB4B0A609AC26F7FC1B38D486B8141A8560AE76CAB459A56B1C644A56F7E5C1F8FF3B4BB7CAE410736B960D5A3B99E738958AE83488349FF87AA618203A1FE73408F54CD113378587840255A4C0884F21F698B0BCC1D54733";

    @Test
    public void testParseTransactionRecordWithNewProtocol() {

        SecretKey key = SecureUtil.generateKey("AES", "0123456789abcdef".getBytes(StandardCharsets.UTF_8));
        String encryptHex =
                HexUtil.encodeHexStr(SecureUtil.aes(key.getEncoded()).encrypt(RAW), false);
        System.out.println("rawHex: " + RAW);

        System.out.println("encryptHex: " + encryptHex);

        String format = String.format(TEST_DATA5, encryptHex);

        //        System.out.println("加密报文: " + format);

        EncryptionService encryptionService = new DefaultEncryptionService(key.getEncoded());
        ReflectionProtocolEngine reflectionProtocolEngine =
                new ReflectionProtocolEngine(encryptionService, new ChecksumService());
        ProtocolFieldProcessor protocolFieldProcessor = new ProtocolFieldProcessor(encryptionService);
        reflectionProtocolEngine.setProtocolFieldProcessor(protocolFieldProcessor);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<EncryptedBaseMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        try {
            byte[] bytes = HexUtil.decodeHex(format);

            for (int i = 0; i < 500; i++) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                // 使用协议引擎解析消息
                EncryptedBaseMessage record = engine.parse(byteBuf, EncryptedBaseMessage.class);

                //            log.info("解析结果: {}", record);

                ValidationResult validation = engine.validate(record);
                if (validation.valid()) {
                    //                log.info("消息验证通过");
                } else {
                    log.warn("消息验证失败: {}", validation.message());
                }

                //            logKeyFields(record);
                //            log.info("解析功能验证完成，数据解析正常");

                ByteBuf serializeBuffer = Unpooled.buffer();
                engine.serialize(record, serializeBuffer);

                // 如果到达这里，说明序列化成功了
                byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
                serializeBuffer.readBytes(serializedBytes);
                //            log.info("序列化报文： {}", HexUtil.encodeHexStr(serializedBytes, false));
                stopWatch.stop();
                log.info("结果: time {}", stopWatch.getTotalTimeNanos());
            }
        } catch (ProtocolException e) {
            log.error("协议解析异常: {}", e.getMessage(), e);
            throw new RuntimeException("协议解析失败", e);
        } catch (Exception e) {
            log.error("解析过程中发生未知异常", e);
            throw new RuntimeException("解析失败", e);
        }
    }

    /**
     * 输出关键字段信息
     */
    private void logKeyFields(EncryptedBaseMessage record) {
        log.info("=== 关键字段信息 ===");
        log.info("帧类型: {}", record.getFrameType());
        log.info("订单编号: {}", record.getInnerRecord().getOrderNumber());
        log.info("桩编号: {}", record.getInnerRecord().getStationNumber());
        log.info("枪号: {}", record.getInnerRecord().getGunNumber());
        log.info("开始时间: {}", record.getInnerRecord().getStartTime());
        log.info("结束时间: {}", record.getInnerRecord().getEndTime());

        log.info("=== 尖时段数据 ===");
        log.info("尖单价: {}", record.getInnerRecord().getPeakPrice());
        log.info("尖电量: {}", record.getInnerRecord().getPeakElectricity());
        log.info("尖金额: {}", record.getInnerRecord().getPeakAmount());

        log.info("=== 峰时段数据 ===");
        log.info("峰单价: {}", record.getInnerRecord().getHighPrice());
        log.info("峰电量: {}", record.getInnerRecord().getHighElectricity());
        log.info("峰金额: {}", record.getInnerRecord().getHighAmount());

        log.info("=== 平时段数据 ===");
        log.info("平单价: {}", record.getInnerRecord().getNormalPrice());
        log.info("平电量: {}", record.getInnerRecord().getNormalElectricity());
        log.info("平金额: {}", record.getInnerRecord().getNormalAmount());

        log.info("=== 谷时段数据 ===");
        log.info("谷单价: {}", record.getInnerRecord().getValleyPrice());
        log.info("谷电量: {}", record.getInnerRecord().getValleyElectricity());
        log.info("谷金额: {}", record.getInnerRecord().getValleyAmount());

        log.info("=== 总计信息 ===");
        log.info("消费金额: {}", record.getInnerRecord().getTotalAmount());
        log.info("VIN: {}", record.getInnerRecord().getVIN());
        log.info("交易标识: {}", record.getInnerRecord().getTag());
        log.info("交易日期、时间: {}", record.getInnerRecord().getTIME());
        log.info("停止原因: {}", record.getInnerRecord().getDesc());
        log.info("物理卡号: {}", record.getInnerRecord().getCard());

        log.info("=== 总计信息 ===");
        log.info("CRC: {}", record.getChecksum());
    }
}
