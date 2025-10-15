package com.lambda.cloud.netty.message;

import cn.hutool.core.io.checksum.CRC16;
import com.lambda.cloud.netty.protocol.checksum.LambdaCRC16Modbus;
import com.lambda.cloud.netty.protocol.packet.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SegmentTest {

    // 测试数据包
    String testData =
            "686600420085181200000001700017c0d40100204e0000289a0100204e0000880d010050460000d0840000803e0000d8590000204e00000002020202020202020202020202020202020202020303040404040404030302020101000000000101010102020202020279DC";

    String testData2 = "682A022500F018120000000001011D68747470733A2F2F686C637863782E6875613030312E6E65743F69643D819B";

    String testData3 =
            "68B217F3003B1812000000017001159733514595532818120000000170019065170A0A0A19E880060B0A0A19E022020000000000000000000000000048E80100000000000000000000000000D853010000000000000000000000000050C300007E4E01000000000030A70000F8A700001E320000000000001815000076A91E0800122A2008009C8001000000000048BC00004C4233373041444E32534A31383832383801E880060B0A0A194002607F8F42D86D1BBD5A";
    //    String testData3 =
    // "68B220EC003B18120000000170011597535818956800181200000001700148710E0B0A0A1998B7100C0A0A19E022020000000000000000000000000048E80100000000000000000000000000D853010000000000000000000000000050C30000000000000000000000000000F8A700002E86050000000000D05F0200122A20080040B02508002E86050000000000D05F02004C565043364434433853443030383635340198B7100C0A0A194102603FD2F0E36D1B3EAE";

    @Test
    void test0() {
        String hexData = "022500F018120000000001011D68747470733A2F2F686C637863782E6875613030312E6E65743F69643D";
        CRC16 crc16 = new CRC16(new LambdaCRC16Modbus());
        crc16.update(UnitKit.hexToBytes(hexData));
        String hexValue = crc16.getHexValue(true);
        Ascii crc = new Ascii(4).setLittleEnd(true);
        crc.setData(hexValue);
        log.info("crc16: {}", crc.getData());
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

    @Test
    void test2() {
        log.info("开始测试订单包解析");
        log.info("订单包数据: {}", testData3);
        log.info("订单包数据长度: {} 字符 ({} 字节)", testData3.length(), testData3.length() / 2);

        byte[] bytes = UnitKit.hexToBytes(testData3);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        try {
            // 跳过报文头部：68(起始) 5E(长度) 00 25(序号) 00(加密) 58(关键字) = 6字节
            byteBuf.skipBytes(4);

            Hex data = new Hex(2);
            data.read(byteBuf);
            log.info("类型: {}", data.getHexData());

            data = new Hex(16);
            data.read(byteBuf);
            log.info("订单编号: {}", data.getHexData());

            data = new Hex(7);
            data.read(byteBuf);
            log.info("桩编号: {}", data.getHexData());

            data = new Hex(1);
            data.read(byteBuf);
            log.info("枪: {}", data.getHexData());

            data = new Hex(7);
            data.read(byteBuf);
            log.info("开始时间: {}", data.getHexData());

            data = new Hex(7);
            data.read(byteBuf);
            log.info("结束时间: {}", data.getHexData());

            // ***************************
            Hex data1 = new Hex(4, 5);
            data1.setLittleEnd(false).read(byteBuf);
            log.info("尖单价: {}", data1.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("尖电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("尖损电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("尖金额: {}", data.getData());

            // ***************************

            data = new Hex(4, 5);
            data.setLittleEnd(true).read(byteBuf);
            log.info("峰单价: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("峰电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("峰损电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("峰金额: {}", data.getData());

            // ***************************

            data = new Hex(4, 5);
            data.setLittleEnd(true).read(byteBuf);
            log.info("平单价: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("平电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("平损电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("平金额: {}", data.getData());

            // ***************************

            data = new Hex(4, 5);
            data.setLittleEnd(true).read(byteBuf);
            log.info("谷单价: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("谷电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("谷损电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("谷金额: {}", data.getData());

            // ***************************

            data = new Hex(4, 5);
            data.setLittleEnd(true).read(byteBuf);
            log.info("深谷单价: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("深谷电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("深谷损电量: {}", data.getData());

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("深谷金额: {}", data.getData());
            byteBuf.skipBytes(18);

            data = new Hex(4, 4);
            data.setLittleEnd(true).read(byteBuf);
            log.info("消费金额: {}", data.getData());

        } finally {
            byteBuf.release();
        }
    }

    @Test
    public void test4() {
        //        BigDecimal chargingPrice = BigDecimal.valueOf(0.34000);
        //        BigDecimal servicePrice =BigDecimal.valueOf(0.16000);
        //        BigDecimal totalPrice = chargingPrice.add(servicePrice);
        //        BigDecimal chargingPriceRatio = BigDecimal.ZERO;
        //        if (totalPrice.compareTo(BigDecimal.ZERO) != 0) {
        //            // 计算平台定义的比例，根据比例将设备的价格重新拆分成电价和服务费价
        //            chargingPriceRatio = chargingPrice.divide(totalPrice, 6, RoundingMode.HALF_UP);
        //            chargingPrice = chargeAndService.multiply(chargingPriceRatio).setScale(3, RoundingMode.HALF_UP);
        ////            servicePrice = chargeAndService.subtract(chargingPrice);
        //        }
    }
}
