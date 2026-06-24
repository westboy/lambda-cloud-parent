package com.lambda.cloud.netty.protocol.message;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
public class RawRecordProtocolTest {

    /**
     * testData3 - 0x3B帧类型的交易记录报文
     */
    private static final String TEST_DATA6 =
            "68B20237003B18120000000001012667370472554496181200000000010168BF2E0911041A90E2050A11041AC8260200FAAA00000000000004F10000C051020000000000000000000000000068B3020000000000000000000000000088840200000000000000000000000000A8D801000000000000000000000000005263E802004C0EE90200FAAA00000000000004F100004C465A36324157355853443033383530300390E2050A11041A4100000000000000002F67";

    private static final String TEST_DATA5 =
            "68A2EFA9003B18120000003155013007745987919872181200000031550108CF1D0C50061AE0AB2B0C50061AB2050200000000000000000000000000FFD0010000000000000000000000000072640100EAE70A00EAE70A009FF30900EEF7000000000000000000000000000046CEAA180030B6B51800EAE70A00EAE70A009DF309004C3538344334564330534430303338373501E0AB2B0C50061A8A85F475B23F99BF1BEAA1";

    private static final String TEST_DATA4 =
            "68A2077A003B181200000002660116430696294154241812000000026601204E0E0C120A19F0D21C0D120A19000000006879370000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009AA9534F0002238B4F006879370000000000000000004C4741473450593333533630313332333501F0D21C0D120A19410150BE785B3E781B4733";

    private static final String TEST_DATA2 =
            "68B2039F003B18120000000424012463640846090240181200000004240180BB0A0A0C031AD00727050C031A68360200000000000000000000000000B8FF0100000000000000000000000000486B010084EC1600000000006C51150030F2000096A0050000000000947C0300D8D60000000000000000000000000000803280EA009ABF9CEA001A8D1C000000000000CE18004C4331484D59424633533030313437303003D00727050C031A41000000000000000053A4";

    @Test
    public void testParseTransactionRecordWithNewProtocol() throws InterruptedException {
        //        log.info("开始使用新协议框架解析交易记录报文");
        //        log.info("测试数据: {}", TEST_DATA4);
        //        log.info("数据长度: {} 字符 ({} 字节)", TEST_DATA4.length(), TEST_DATA4.length() / 2);

        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<RawBaseMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        Thread.sleep(2000);
        extracted1(engine, TEST_DATA5);
        //        Thread.sleep(1000);
        //        extracted1(engine, TEST_DATA4);
        //        Thread.sleep(1000);
        //        extracted1(engine, TEST_DATA6);
    }

    private void extracted1(ProtocolEngine<RawBaseMessage> engine, String TEST_DATA5) {
        try {
            //            for (int i = 0; i < 1000; i++) {
            // 先获取消息元数据，检查预期长度
            var metadata = engine.getMetadata(RawBaseMessage.class);
            //                log.info("消息预期总长度: {} 字节", metadata.totalLength());
            //                log.info("字段数量: {}", metadata.fields().size());

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(TEST_DATA5);
            //                log.info("实际数据长度: {} 字节", bytes.length);

            if (bytes.length < metadata.totalLength()) {
                log.warn("数据长度不足！实际: {} 字节，预期: {} 字节", bytes.length, metadata.totalLength());
            }

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            long current = System.currentTimeMillis();
            //                log.info("开始解析报文...");
            // 使用协议引擎解析消息
            RawBaseMessage record = engine.parse(byteBuf, RawBaseMessage.class);

            // 验证解析结果
            ValidationResult validation = engine.validate(record);
            //                if (validation.valid()) {
            //                    log.info("消息验证通过");
            //                } else {
            //                    log.warn("消息验证失败: {}", validation.message());
            //                }

            // 输出关键字段
            logKeyFields(record);
            //                log.info("解析功能验证完成，数据解析正常");

            long stop = System.currentTimeMillis();
            log.info("解析耗时: time {}  {}", (stop - current));
            record.setChecksum(null);
            ByteBuf serializeBuffer = Unpooled.buffer();
            record.getInnerRecord().setVIN("LGAG4PY33S6013235");
            engine.serialize(record, serializeBuffer);
            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);
            log.info("序列化报文： {}", HexUtil.encodeHexStr(serializedBytes, false));
            //            }

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
    private void logKeyFields(RawBaseMessage record) {
        System.out.println("=== 关键字段信息 ===");
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
        //
        //        log.info("=== 深谷时段数据 ===");
        //        log.info("深谷单价: {}", record.getInnerRecord().getDeepValleyPrice());
        //        log.info("深谷电量: {}", record.getInnerRecord().getDeepValleyElectricity());
        //        log.info("深谷金额: {}", record.getInnerRecord().getDeepValleyAmount());

        log.info("=== 总计信息 ===");
        log.info("消费金额: {}", record.getInnerRecord().getTotalAmount());

        log.info("=== 电表总起值 ===");
        log.info("电表总起值: {}", record.getInnerRecord().getS1());
        log.info("电表总起值: {}", record.getInnerRecord().getS2());

        log.info("=== 电表总起值 ===");
        log.info("电表电量: {}", record.getInnerRecord().getReserved0());
        log.info("电表损耗: {}", record.getInnerRecord().getReserved1());

        log.info("VIN: {}", record.getInnerRecord().getVIN());

        log.info("交易标识: {}", record.getInnerRecord().getTag());
        log.info("交易日期、时间: {}", record.getInnerRecord().getTIME());
        log.info("停止原因: {}", record.getInnerRecord().getDesc());
        log.info("物理卡号: {}", record.getInnerRecord().getCard());

        log.info("=== 总计信息 ===");
        log.info("CRC: {}", record.getChecksum());
    }
}
