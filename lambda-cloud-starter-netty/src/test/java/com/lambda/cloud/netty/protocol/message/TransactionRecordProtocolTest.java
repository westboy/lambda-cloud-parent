package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.ProtocolEngine;
import com.lambda.cloud.netty.protocol.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.ProtocolException;
import com.lambda.cloud.netty.protocol.packet.UnitKit;
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
public class TransactionRecordProtocolTest {

    /**
     * testData3 - 0x3B帧类型的交易记录报文
     */
    private static final String TEST_DATA3 =
            "68B217F3003B1812000000017001159733514595532818120000000170019065170A0A0A19E880060B0A0A19E022020000000000000000000000000048E80100000000000000000000000000D853010000000000000000000000000050C300007E4E01000000000030A70000F8A700001E320000000000001815000076A91E0800122A2008009C8001000000000048BC00004C4233373041444E32534A31383832383801E880060B0A0A194002607F8F42D86D1BBD5A";

    @Test
    public void testParseTransactionRecordWithNewProtocol() {
        log.info("开始使用新协议框架解析交易记录报文");
        log.info("测试数据: {}", TEST_DATA3);
        log.info("数据长度: {} 字符 ({} 字节)", TEST_DATA3.length(), TEST_DATA3.length() / 2);

        // 获取协议引擎
        ProtocolEngine<TransactionRecord> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            // 先获取消息元数据，检查预期长度
            var metadata = engine.getMetadata(TransactionRecord.class);
            log.info("消息预期总长度: {} 字节", metadata.totalLength());
            log.info("字段数量: {}", metadata.fields().size());

            // 将十六进制字符串转换为字节数组
            byte[] bytes = UnitKit.hexToBytes(TEST_DATA3);
            log.info("实际数据长度: {} 字节", bytes.length);

            if (bytes.length < metadata.totalLength()) {
                log.warn("数据长度不足！实际: {} 字节，预期: {} 字节", bytes.length, metadata.totalLength());
            }

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

            log.info("开始解析报文...");

            // 使用协议引擎解析消息
            TransactionRecord record = engine.parse(byteBuf, TransactionRecord.class);

            log.info("解析成功！");
            log.info("解析结果: {}", record);

            // 验证解析结果
            ProtocolEngine.ValidationResult validation = engine.validate(record);
            if (validation.valid()) {
                log.info("消息验证通过");
            } else {
                log.warn("消息验证失败: {}", validation.message());
            }

            // 输出关键字段
            logKeyFields(record);

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
    private void logKeyFields(TransactionRecord record) {
        log.info("=== 关键字段信息 ===");
        log.info("帧类型: {}", record.getFrameType());
        log.info("订单编号: {}", record.getOrderNumber());
        log.info("桩编号: {}", record.getStationNumber());
        log.info("枪号: {}", record.getGunNumber());
        log.info("开始时间: {}", record.getStartTime());
        log.info("结束时间: {}", record.getEndTime());

        log.info("=== 尖时段数据 ===");
        log.info("尖单价: {}", record.getPeakPrice());
        log.info("尖电量: {}", record.getPeakElectricity());
        log.info("尖金额: {}", record.getPeakAmount());

        log.info("=== 峰时段数据 ===");
        log.info("峰单价: {}", record.getHighPrice());
        log.info("峰电量: {}", record.getHighElectricity());
        log.info("峰金额: {}", record.getHighAmount());

        log.info("=== 平时段数据 ===");
        log.info("平单价: {}", record.getNormalPrice());
        log.info("平电量: {}", record.getNormalElectricity());
        log.info("平金额: {}", record.getNormalAmount());

        log.info("=== 谷时段数据 ===");
        log.info("谷单价: {}", record.getValleyPrice());
        log.info("谷电量: {}", record.getValleyElectricity());
        log.info("谷金额: {}", record.getValleyAmount());

        log.info("=== 深谷时段数据 ===");
        log.info("深谷单价: {}", record.getDeepValleyPrice());
        log.info("深谷电量: {}", record.getDeepValleyElectricity());
        log.info("深谷金额: {}", record.getDeepValleyAmount());

        log.info("=== 总计信息 ===");
        log.info("消费金额: {}", record.getTotalAmount());
        log.info("校验码: {}", record.getChecksum());
    }

    @Test
    public void testSerializeTransactionRecord() {
        log.info("测试交易记录序列化功能");

        // 获取协议引擎
        ProtocolEngine<TransactionRecord> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            // 先解析原始数据
            byte[] bytes = UnitKit.hexToBytes(TEST_DATA3);
            ByteBuf sourceBuf = Unpooled.wrappedBuffer(bytes);
            TransactionRecord record = engine.parse(sourceBuf, TransactionRecord.class);

            // 序列化回字节数组
            ByteBuf targetBuf = Unpooled.buffer();
            engine.serialize(record, targetBuf);

            // 转换为十六进制字符串
            byte[] serializedBytes = new byte[targetBuf.readableBytes()];
            targetBuf.readBytes(serializedBytes);
            String serializedHex = UnitKit.bytesToHex(serializedBytes).toUpperCase();

            log.info("原始数据: {}", TEST_DATA3);
            log.info("序列化后: {}", serializedHex);
            log.info("数据一致性: {}", TEST_DATA3.equals(serializedHex) ? "一致" : "不一致");

            targetBuf.release();

        } catch (Exception e) {
            log.error("序列化测试失败", e);
            throw new RuntimeException("序列化测试失败", e);
        }
    }

    @Test
    public void testProtocolEngineMetadata() {
        log.info("测试协议引擎元数据功能");

        ProtocolEngine<TransactionRecord> engine = ProtocolEngineFactory.getDefaultEngine();

        // 获取消息元数据
        var metadata = engine.getMetadata(TransactionRecord.class);

        log.info("消息类型: {}", metadata.getMessageType());
        log.info("消息名称: {}", metadata.getMessageName());
        log.info("消息描述: {}", metadata.getDescription());
        log.info("消息总长度: {} 字节", metadata.totalLength());
        log.info("字段数量: {}", metadata.fields().size());

        // 输出字段信息
        log.info("=== 字段详情 ===");
        metadata.fields().forEach(field -> {
            log.info(
                    "字段[{}]: {} - {} ({} 字节, {})",
                    field.getOrder(),
                    field.getFieldName(),
                    field.getDescription(),
                    field.getLength(),
                    field.getDataType());
        });
    }
}
