package com.lambda.cloud.netty.protocol.message.deep;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
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
    private static final String TEST_DATA3 =
            "68B217F3003B1812000000017001159733514595532818120000000170019065170A0A0A19E880060B0A0A19E022020000000000000000000000000048E80100000000000000000000000000D853010000000000000000000000000050C300007E4E01000000000030A70000F8A700001E320000000000001815000076A91E0800122A2008009C8001000000000048BC00004C4233373041444E32534A31383832383801E880060B0A0A194002607F8F42D86D1BBD5A";

    private static final String TEST_DATA6 =
            "68B29006003B777711111111110117859028974796807777111111111101D00727116C0B1990E229116C0B19D0FB0100000000000000000000000000D0FB0100000000000000000000000000000000000000000000000000000000006F8203000000000000000000000000000000000000000000000000000000000080A55C00001AB15C00009A0B00009A0B0000AE1A000030303030300000000000000000000000000190E229116C0B1940136075D8C446981B187A";
    private static final String TEST_DATA7 =
            "68B29603003B777711111111110117857181618503687777111111111101C0DA33100C0B1968BF34100C0B19D0FB010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000006F82030000000000000000000000000000000000000000000000000000000000F47C5C0000747F5C00008002000080020000BE05000030303030300000000000000000000000000168BF34100C0B1940106075D8C446981B0096";

    @Test
    public void testParseTransactionRecordWithNewProtocol() {
        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine(null);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<RawBaseMessage> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        try {
            // 先获取消息元数据，检查预期长度
            var metadata = engine.getMetadata(RawBaseMessage.class);

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(TEST_DATA6);

            if (bytes.length < metadata.totalLength()) {
                log.warn("数据长度不足！实际: {} 字节，预期: {} 字节", bytes.length, metadata.totalLength());
            }

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            long current = System.currentTimeMillis();
            RawBaseMessage record = engine.parse(byteBuf, RawBaseMessage.class);

            // 输出关键字段
            logKeyFields(record);

            long stop = System.currentTimeMillis();
            log.info("解析耗时: time {}  {}", (stop - current), record);
            record.setChecksum(null);
            ByteBuf serializeBuffer = Unpooled.buffer();
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

        log.info("=== 深谷时段数据 ===");
        log.info("深谷单价: {}", record.getInnerRecord().getDeepValleyPrice());
        log.info("深谷电量: {}", record.getInnerRecord().getDeepValleyElectricity());
        log.info("深谷金额: {}", record.getInnerRecord().getDeepValleyAmount());

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
