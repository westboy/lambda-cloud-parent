package com.lambda.cloud.netty.protocol.secure;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverterFactory;
import com.lambda.cloud.netty.protocol.encryption.EncryptionService;
import com.lambda.cloud.netty.protocol.encryption.impl.DefaultEncryptionService;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.processor.ProtocolFieldProcessor;
import com.lambda.cloud.netty.protocol.validation.ValidationResult;
import com.lambda.cloud.netty.utils.HexUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private static final String TEST_DATA5 = "68A2077A003B%s4733";

    @Test
    public void testParseTransactionRecordWithNewProtocol() {
        log.info("开始使用新协议框架解析交易记录报文");
        log.info("测试数据: {}", TEST_DATA4);
        log.info("数据长度: {} 字符 ({} 字节)", TEST_DATA4.length(), TEST_DATA4.length() / 2);

        SecretKey key = SecureUtil.generateKey("AES", 128);
        String encryptHex = HexUtil.encodeHexStr(SecureUtil.aes(key.getEncoded())
                .encrypt(RAW),false) ;
        System.out.println("rawHex: "+RAW);

        System.out.println("encryptHex: "+encryptHex);

        String format = String.format(TEST_DATA5, encryptHex);

        System.out.println("加密报文: "+format);

        EncryptionService encryptionService = new DefaultEncryptionService(key.getEncoded());

        ReflectionProtocolEngine reflectionProtocolEngine = new ReflectionProtocolEngine();
        reflectionProtocolEngine.setConverterFactory(new DataTypeConverterFactory(encryptionService));
        ProtocolFieldProcessor protocolFieldProcessor = new ProtocolFieldProcessor(encryptionService);
        reflectionProtocolEngine.setProtocolFieldProcessor(protocolFieldProcessor);
        ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, reflectionProtocolEngine);
        // 获取协议引擎
        ProtocolEngine<EncryptedBaseMessage> engine = ProtocolEngineFactory.getDefaultEngine();

        try {
            byte[] bytes = HexUtil.decodeHex(format);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

            log.info("开始解析报文...");

            EncryptedBaseMessage record = engine.parse(byteBuf, EncryptedBaseMessage.class);

            log.info("解析结果: {}", record);

            ValidationResult validation = engine.validate(record);
            if (validation.valid()) {
                log.info("消息验证通过");
            } else {
                log.warn("消息验证失败: {}", validation.message());
            }

            logKeyFields(record);
            log.info("解析功能验证完成，数据解析正常");

            ByteBuf serializeBuffer = Unpooled.buffer();
            engine.serialize(record, serializeBuffer);

            // 如果到达这里，说明序列化成功了
            byte[] serializedBytes = new byte[serializeBuffer.readableBytes()];
            serializeBuffer.readBytes(serializedBytes);

            String userNameById = HexUtil.encodeHexStr(serializedBytes,false);
            System.out.println(userNameById);


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
