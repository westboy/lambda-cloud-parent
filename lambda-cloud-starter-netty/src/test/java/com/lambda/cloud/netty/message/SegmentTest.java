package com.lambda.cloud.netty.message;

import cn.hutool.core.io.checksum.CRC16;
import com.lambda.cloud.netty.protocol.checksum.LambdaCRC16Modbus;
import com.lambda.cloud.netty.protocol.packet.Bcd;
import com.lambda.cloud.netty.protocol.packet.Hex;
import com.lambda.cloud.netty.protocol.packet.UnitKit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SegmentTest {

    // 测试数据包
    String testData =
            "685e0002005818120000001294003aa08601008038010068360200204e000098ab0200204e0000e09f0200204e00000000000000000000000000000000000000010101010101010102020202020202020202020203030303030303030303030380EC";

    String testData2 = "682A022500F018120000000001011D68747470733A2F2F686C637863782E6875613030312E6E65743F69643D819B";

    @Test
    void test0() {
        String hexData = "022500F018120000000001011D68747470733A2F2F686C637863782E6875613030312E6E65743F69643D";
        CRC16 crc16 = new CRC16(new LambdaCRC16Modbus(true));
        crc16.update(UnitKit.hexToBytes(hexData));
        String hexValue = crc16.getHexValue(true);
        log.info("crc16: {}", hexValue);
    }

    @Test
    void test1() {
        log.info("开始测试数据包解析");
        log.info("测试数据: {}", testData);
        log.info("数据长度: {} 字符 ({} 字节)", testData.length(), testData.length() / 2);

        byte[] bytes = UnitKit.hexToBytes(testData);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        try {
            // 跳过报文头部：68(起始) 5E(长度) 00 25(序号) 00(加密) 58(关键字) = 6字节
            byteBuf.skipBytes(6);

            // 1. 解析帧编号 (6字节BCD，根据协议文档)
            Bcd frameNumberBcd = new Bcd(7);
            frameNumberBcd.read(byteBuf);
            log.info("桩编号: {}", frameNumberBcd.getData());

            // 2. 解析计费费率编码 (2字节BCD)
            Hex billingRateCodeBcd = new Hex(2);
            billingRateCodeBcd.read(byteBuf);
            log.info("计费费率编码: {}", billingRateCodeBcd.getData());

            // 3. 解析尖电量费率 (4字节BIN，精确到5位小数)
            Hex peakElectricityRateHex = new Hex(4, 5);
            peakElectricityRateHex.setLittleEnd(true);
            peakElectricityRateHex.read(byteBuf);
            log.info("尖电量费率: {}", peakElectricityRateHex.getData());

            // 4. 解析峰电量费率 (4字节BIN，精确到5位小数)
            Hex highElectricityRateHex = new Hex(4, 5);
            highElectricityRateHex.setLittleEnd(true);
            highElectricityRateHex.read(byteBuf);
            log.info("峰电量费率: {}", highElectricityRateHex.getData());

            // 5. 解析峰电费费率 (4字节BIN，精确到5位小数)
            Hex highElectricityFeeRateHex = new Hex(4, 5);
            highElectricityFeeRateHex.setLittleEnd(true);
            highElectricityFeeRateHex.read(byteBuf);
            log.info("峰电费费率: {}", highElectricityFeeRateHex.getData());

            // 6. 解析峰服务费率 (4字节BIN，精确到5位小数)
            Hex highServiceFeeRateHex = new Hex(4, 5);
            highServiceFeeRateHex.setLittleEnd(true);
            highServiceFeeRateHex.read(byteBuf);
            log.info("峰服务费率: {}", highServiceFeeRateHex.getData());

            // 7. 解析平电费费率 (4字节BIN，精确到5位小数)
            Hex normalElectricityFeeRateHex = new Hex(4, 5);
            normalElectricityFeeRateHex.setLittleEnd(true);
            normalElectricityFeeRateHex.read(byteBuf);
            log.info("平电费费率: {}", normalElectricityFeeRateHex.getData());

            // 8. 解析平服务费率 (4字节BIN，精确到5位小数)
            Hex normalServiceFeeRateHex = new Hex(4, 5);
            normalServiceFeeRateHex.setLittleEnd(true);
            normalServiceFeeRateHex.read(byteBuf);
            log.info("平服务费率: {}", normalServiceFeeRateHex.getData());

            // 9. 解析谷电费费率 (4字节BIN，精确到5位小数)
            Hex valleyElectricityFeeRateHex = new Hex(4, 5);
            valleyElectricityFeeRateHex.setLittleEnd(true);
            valleyElectricityFeeRateHex.read(byteBuf);
            log.info("谷电费费率: {}", valleyElectricityFeeRateHex.getData());

            // 10. 解析谷服务费率 (4字节BIN，精确到5位小数)
            Hex valleyServiceFeeRateHex = new Hex(4, 5);
            valleyServiceFeeRateHex.setLittleEnd(true);
            valleyServiceFeeRateHex.read(byteBuf);
            log.info("谷服务费率: {}", valleyServiceFeeRateHex.getData());

            // 9. 解析谷电费费率 (4字节BIN，精确到5位小数)
            Hex deepElectricityFeeRateHex = new Hex(4, 5);
            deepElectricityFeeRateHex.setLittleEnd(true);
            deepElectricityFeeRateHex.read(byteBuf);
            log.info("深谷服务费率: {}", deepElectricityFeeRateHex.getData());

            // 10. 解析谷服务费率 (4字节BIN，精确到5位小数)
            Hex deepServiceFeeRateHex = new Hex(4, 5);
            deepServiceFeeRateHex.setLittleEnd(true);
            deepServiceFeeRateHex.read(byteBuf);
            log.info("深谷服务费率: {}", deepServiceFeeRateHex.getData());

            // 11. 解析计费比例 (1字节BIN)
            Hex billingRatioHex = new Hex(1);
            billingRatioHex.read(byteBuf);
            log.info("计费比例: {}", billingRatioHex.getData());

            // 12. 解析48个时段费率号 (每个1字节BIN)
            for (int i = 0; i < 48; i++) {
                Hex timeSlotRateHex = new Hex(1);
                timeSlotRateHex.read(byteBuf);
            }
            log.info("时段费率号解析完成，共48个时段");

        } catch (Exception e) {
            log.error("解析电表数据包时发生错误", e);
            throw new RuntimeException("解析电表数据包失败", e);
        } finally {
            byteBuf.release();
        }
    }
}
