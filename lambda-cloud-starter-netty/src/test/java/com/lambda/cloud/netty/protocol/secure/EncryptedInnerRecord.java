package com.lambda.cloud.netty.protocol.secure;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ProtocolFrame(frameType = "0x3B", name = "交易记录", description = "消息体", version = "1.0")
public class EncryptedInnerRecord {

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.HEX, description = "订单编号")
    private String orderNumber;

    /**
     * 桩编号 (7字节)
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.HEX, description = "桩编号")
    private String stationNumber;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer gunNumber;

    /**
     * 开始时间 (7字节 CP56Time2a格式)
     */
    @ProtocolField(order = 4, length = 7, computed = true, dataType = ProtocolDataType.CP56TIME2A, description = "开始时间")
    private String startTime;

    /**
     * 结束时间 (7字节 CP56Time2a格式)
     */
    @ProtocolField(order = 5, length = 7, computed = true, dataType = ProtocolDataType.CP56TIME2A, description = "结束时间")
    private String endTime;

    // 尖时段数据
    /**
     * 尖单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "尖单价")
    private BigDecimal peakPrice;

    /**
     * 尖电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 7,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "尖电量")
    private BigDecimal peakElectricity;

    /**
     * 尖损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 8,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "尖损电量")
    private BigDecimal peakLossElectricity;

    /**
     * 尖金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 9,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "尖金额")
    private BigDecimal peakAmount;

    // 峰时段数据
    /**
     * 峰单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 10,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "峰单价")
    private BigDecimal highPrice;

    /**
     * 峰电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 11,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "峰电量")
    private BigDecimal highElectricity;

    /**
     * 峰损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 12,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "峰损电量")
    private BigDecimal highLossElectricity;

    /**
     * 峰金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 13,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "峰金额")
    private BigDecimal highAmount;

    // 平时段数据
    /**
     * 平单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 14,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "平单价")
    private BigDecimal normalPrice;

    /**
     * 平电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 15,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "平电量")
    private BigDecimal normalElectricity;

    /**
     * 平损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 16,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "平损电量")
    private BigDecimal normalLossElectricity;

    /**
     * 平金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 17,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "平金额")
    private BigDecimal normalAmount;

    // 谷时段数据
    /**
     * 谷单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 18,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "谷单价")
    private BigDecimal valleyPrice;

    /**
     * 谷电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 19,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷电量")
    private BigDecimal valleyElectricity;

    /**
     * 谷损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 20,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷损电量")
    private BigDecimal valleyLossElectricity;

    /**
     * 谷金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 21,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷金额")
    private BigDecimal valleyAmount;

    /**
     * 预留字段 (18字节)
     */
    @ProtocolField(order = 26, length = 18, computed = true, dataType = ProtocolDataType.HEX, description = "预留字段")
    private String reserved;

    /**
     * 消费金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 27,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "消费金额")
    private BigDecimal totalAmount;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 28, length = 17, computed = true, dataType = ProtocolDataType.ASCII, description = "帧类型")
    private String VIN;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 29, length = 1, computed = true, dataType = ProtocolDataType.HEX, description = "帧类型")
    private String tag;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 30, length = 7, computed = true, dataType = ProtocolDataType.CP56TIME2A, description = "帧类型")
    private String TIME;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 31, length = 1, computed = true, dataType = ProtocolDataType.HEX, description = "帧类型")
    private String desc;
    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 32, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "帧类型")
    private String card;
}
