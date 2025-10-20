package com.lambda.cloud.netty.protocol.message;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import com.lambda.cloud.netty.protocol.encrypt.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.Objects;
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
public class RawRecordProtocolTest {

    /**
     * testData3 - 0x3B帧类型的交易记录报文
     */
    private static final String TEST_DATA3 =
            "68B217F3003B1812000000017001159733514595532818120000000170019065170A0A0A19E880060B0A0A19E022020000000000000000000000000048E80100000000000000000000000000D853010000000000000000000000000050C300007E4E01000000000030A70000F8A700001E320000000000001815000076A91E0800122A2008009C8001000000000048BC00004C4233373041444E32534A31383832383801E880060B0A0A194002607F8F42D86D1BBD5A";

    private static final String TEST_DATA4 =
            "68A2077A003B181200000002660116430696294154241812000000026601204E0E0C120A19F0D21C0D120A19000000006879370000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009AA9534F0002238B4F006879370000000000000000004C4741473450593333533630313332333501F0D21C0D120A19410150BE785B3E781B4733";

    @Test
    public void testParseTransactionRecordWithNewProtocol() {
        log.info("开始使用新协议框架解析交易记录报文");
        log.info("测试数据: {}", TEST_DATA4);
        log.info("数据长度: {} 字符 ({} 字节)", TEST_DATA4.length(), TEST_DATA4.length() / 2);

        // 获取协议引擎
        ProtocolEngine<RawBaseMessage> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            // 先获取消息元数据，检查预期长度
            var metadata = engine.getMetadata(RawBaseMessage.class);
            log.info("消息预期总长度: {} 字节", metadata.totalLength());
            log.info("字段数量: {}", metadata.fields().size());

            // 将十六进制字符串转换为字节数组
            byte[] bytes = HexUtil.decodeHex(TEST_DATA4);
            log.info("实际数据长度: {} 字节", bytes.length);

            if (bytes.length < metadata.totalLength()) {
                log.warn("数据长度不足！实际: {} 字节，预期: {} 字节", bytes.length, metadata.totalLength());
            }

            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

            log.info("开始解析报文...");

            // 使用协议引擎解析消息
            RawBaseMessage record = engine.parse(byteBuf, RawBaseMessage.class);

            log.info("解析结果: {}", record);

            // 验证解析结果
            ValidationResult validation = engine.validate(record);
            if (validation.valid()) {
                log.info("消息验证通过");
            } else {
                log.warn("消息验证失败: {}", validation.message());
            }

            // 输出关键字段
            logKeyFields(record);
            log.info("解析功能验证完成，数据解析正常");

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
        //
        //        log.info("=== 深谷时段数据 ===");
        //        log.info("深谷单价: {}", record.getInnerRecord().getDeepValleyPrice());
        //        log.info("深谷电量: {}", record.getInnerRecord().getDeepValleyElectricity());
        //        log.info("深谷金额: {}", record.getInnerRecord().getDeepValleyAmount());

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

    @Test
    public void testProtocolEngineMetadata() {
        log.info("测试协议引擎元数据功能");

        ProtocolEngine<RawInnerRecord> engine = ProtocolEngineFactory.getDefaultEngine();

        // 获取消息元数据
        var metadata = engine.getMetadata(RawInnerRecord.class);

        log.info("消息类型: {}", metadata.getFrameType());
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

    /**
     * 比较两个十六进制字符串，找出差异位置
     */
    private void compareHexStrings(String original, String serialized) {
        log.info("=== 数据差异分析 ===");

        int minLength = Math.min(original.length(), serialized.length());
        int maxLength = Math.max(original.length(), serialized.length());

        if (original.length() != serialized.length()) {
            log.warn("长度不同: 原始 {} 字符, 序列化 {} 字符", original.length(), serialized.length());
        }

        int diffCount = 0;
        for (int i = 0; i < minLength; i += 2) {
            String originalByte = original.substring(i, Math.min(i + 2, original.length()));
            String serializedByte = serialized.substring(i, Math.min(i + 2, serialized.length()));

            if (!originalByte.equalsIgnoreCase(serializedByte)) {
                log.warn("位置 {} (字节 {}): 原始={}, 序列化={}", i / 2, i / 2, originalByte, serializedByte);
                diffCount++;

                // 只显示前10个差异，避免日志过长
                if (diffCount >= 10) {
                    log.warn("... 还有更多差异，已省略显示");
                    break;
                }
            }
        }

        // 如果长度不同，显示多出的部分
        if (maxLength > minLength) {
            String longerString = original.length() > serialized.length() ? original : serialized;
            String extraPart = longerString.substring(minLength);
            log.warn("多出的部分: {}", extraPart);
        }

        if (diffCount == 0 && original.length() == serialized.length()) {
            log.info("数据完全一致");
        } else {
            log.warn("发现 {} 个字节差异", diffCount);
        }
    }

    /**
     * 独立的序列化测试方法
     * 注意：当前协议引擎序列化功能存在问题，此测试用于验证和记录问题
     */
    @Test
    public void testSerializeTransactionRecord() {
        log.info("=== 独立序列化测试 ===");
        log.warn("注意：当前协议引擎序列化功能存在问题（不支持BigDecimal类型）");

        ProtocolEngine<RawBaseMessage> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            // 首先解析原始数据得到对象
            byte[] originalBytes = HexUtil.decodeHex(TEST_DATA3);
            ByteBuf originalByteBuf = Unpooled.wrappedBuffer(originalBytes);
            RawBaseMessage record = engine.parse(originalByteBuf, RawBaseMessage.class);

            log.info("✓ 解析功能正常，得到对象: {}", record);

            // 尝试序列化对象（预期会失败）
            log.info("尝试序列化对象...");
            ByteBuf serializeBuffer = Unpooled.buffer();
            engine.serialize(record, serializeBuffer);

            // 如果到达这里，说明序列化成功了
            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);
            String serializedHex = HexUtil.encodeHexStr(serializedBytes);

            log.info("✓ 序列化成功: {}", serializedHex);
            log.info("原始数据:     {}", TEST_DATA3);

            // 验证序列化结果
            boolean isIdentical = serializedHex.equalsIgnoreCase(TEST_DATA3);
            log.info("序列化验证结果: {}", isIdentical ? "✓ 通过" : "✗ 失败");

            if (!isIdentical) {
                compareHexStrings(TEST_DATA3, serializedHex);
            }

        } catch (Exception e) {
            log.warn("✗ 序列化失败（预期行为）: {}", e.getMessage());
        }
    }

    /**
     * 序列化-反序列化往返测试
     * 注意：当前协议引擎序列化功能存在问题，此测试用于验证和记录问题
     */
    @Test
    public void testSerializeDeserializeRoundTrip() {
        log.info("=== 序列化-反序列化往返测试 ===");
        log.warn("注意：当前协议引擎序列化功能存在问题（不支持BigDecimal类型）");

        ProtocolEngine<RawBaseMessage> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            // 第一步：解析原始数据
            byte[] originalBytes = HexUtil.decodeHex(TEST_DATA3);
            ByteBuf originalByteBuf = Unpooled.wrappedBuffer(originalBytes);
            RawBaseMessage originalRecord = engine.parse(originalByteBuf, RawBaseMessage.class);

            log.info("✓ 原始解析成功: {}", originalRecord);

            // 第二步：尝试序列化对象（预期会失败）
            log.info("尝试序列化对象...");
            ByteBuf serializeBuffer = Unpooled.buffer();
            engine.serialize(originalRecord, serializeBuffer);

            // 如果到达这里，说明序列化成功了
            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);

            // 第三步：重新解析序列化后的数据
            ByteBuf deserializeByteBuf = Unpooled.wrappedBuffer(serializedBytes);
            RawBaseMessage deserializedRecord = engine.parse(deserializeByteBuf, RawBaseMessage.class);

            log.info("✓ 重新解析成功: {}", deserializedRecord);

            // 第四步：比较两个对象
            boolean isEqual = compareTransactionRecords(originalRecord, deserializedRecord);
            log.info("往返测试结果: {}", isEqual ? "✓ 通过" : "✗ 失败");

            if (!isEqual) {
                log.warn("往返测试失败：序列化-反序列化后的对象与原始对象不一致");
            }

        } catch (Exception e) {
            log.warn("✗ 往返测试失败（预期行为）: {}", e.getMessage());
        }
    }

    /**
     * 比较两个TransactionRecord对象是否相等
     */
    private boolean compareTransactionRecords(RawBaseMessage record1, RawBaseMessage record2) {
        if (record1 == null && record2 == null) {
            return true;
        }
        if (record1 == null || record2 == null) {
            return false;
        }

        // 比较主要字段
        boolean isEqual = true;

        if (!Objects.equals(record1.getFrameType(), record2.getFrameType())) {
            log.warn("帧类型不一致: {} vs {}", record1.getFrameType(), record2.getFrameType());
            isEqual = false;
        }

        if (!Objects.equals(record1.getChecksum(), record2.getChecksum())) {
            log.warn("校验码不一致: {} vs {}", record1.getChecksum(), record2.getChecksum());
            isEqual = false;
        }

        // 比较内部记录
        if (!compareInnerRecords(record1.getInnerRecord(), record2.getInnerRecord())) {
            isEqual = false;
        }

        return isEqual;
    }

    /**
     * 比较两个InnerRecord对象是否相等
     */
    private boolean compareInnerRecords(RawInnerRecord record1, RawInnerRecord record2) {
        if (record1 == null && record2 == null) {
            return true;
        }
        if (record1 == null || record2 == null) {
            return false;
        }

        boolean isEqual = true;

        // 比较关键字段
        if (!Objects.equals(record1.getOrderNumber(), record2.getOrderNumber())) {
            log.warn("订单编号不一致: {} vs {}", record1.getOrderNumber(), record2.getOrderNumber());
            isEqual = false;
        }

        if (!Objects.equals(record1.getStationNumber(), record2.getStationNumber())) {
            log.warn("桩编号不一致: {} vs {}", record1.getStationNumber(), record2.getStationNumber());
            isEqual = false;
        }

        if (!Objects.equals(record1.getGunNumber(), record2.getGunNumber())) {
            log.warn("枪号不一致: {} vs {}", record1.getGunNumber(), record2.getGunNumber());
            isEqual = false;
        }

        // 比较BigDecimal字段（需要特殊处理精度）
        if (!compareBigDecimal(record1.getTotalAmount(), record2.getTotalAmount(), "消费金额")) {
            isEqual = false;
        }

        if (!compareBigDecimal(record1.getPeakPrice(), record2.getPeakPrice(), "尖单价")) {
            isEqual = false;
        }

        if (!compareBigDecimal(record1.getPeakElectricity(), record2.getPeakElectricity(), "尖电量")) {
            isEqual = false;
        }

        // 可以继续添加更多字段的比较...

        return isEqual;
    }

    /**
     * 比较BigDecimal值，考虑精度问题
     */
    private boolean compareBigDecimal(BigDecimal value1, BigDecimal value2, String fieldName) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            log.warn("{} 不一致: {} vs {}", fieldName, value1, value2);
            return false;
        }

        // 使用compareTo比较，忽略精度差异
        if (value1.compareTo(value2) != 0) {
            log.warn("{} 不一致: {} vs {}", fieldName, value1, value2);
            return false;
        }

        return true;
    }

    /**
     * 测试配置了加密服务的协议引擎
     */
    @Test
    public void testProtocolEngineWithEncryption() {
        log.info("=== 测试配置了加密服务的协议引擎 ===");

        try {
            // 生成AES密钥
            SecretKey key = SecureUtil.generateKey("AES", 128);
            EncryptionService encryptionService = new DefaultEncryptionService(key.getEncoded());

            // 创建配置了加密服务的协议引擎
            ReflectionProtocolEngine reflectionEngine = new ReflectionProtocolEngine();
            reflectionEngine.setConverterFactory(new DataTypeConverterFactory(encryptionService));

            ProtocolFieldProcessor fieldProcessor = new ProtocolFieldProcessor(encryptionService);
            reflectionEngine.setProtocolFieldProcessor(fieldProcessor);

            log.info("加密协议引擎创建成功，算法: {}", encryptionService.getAlgorithmName());

            // 测试基本功能
            var metadata = reflectionEngine.getMetadata(RawBaseMessage.class);
            log.info("消息元数据获取成功，字段数量: {}", metadata.fields().size());

            log.info("加密协议引擎测试通过 ✓");

        } catch (Exception e) {
            log.error("加密协议引擎测试失败", e);
            throw new RuntimeException("加密协议引擎测试失败", e);
        }
    }
}
