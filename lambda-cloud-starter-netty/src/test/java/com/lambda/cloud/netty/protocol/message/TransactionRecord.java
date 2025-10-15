package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import java.math.BigDecimal;
import lombok.Data;
import lombok.ToString;

/**
 * 交易记录消息
 * <p>
 * 对应协议帧类型 0x3B，包含充电交易的详细信息
 * </p>
 *
 * @author Jin
 */
@Data
@ToString
@ProtocolFrame(frameType = "0x3B", name = "交易记录", description = "充电交易记录信息", version = "1.0")
public class TransactionRecord {

    /**
     * 报文头 - 起始符 (68)
     */
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX, littleEndian = true, description = "数据长度")
    private String dataLength;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.HEX, littleEndian = true, description = "数据长度")
    private String ser;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.HEX, littleEndian = true, description = "数据长度")
    private String sec;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.HEX, description = "帧类型")
    private String frameType;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.HEX, description = "订单编号")
    private String orderNumber;

    /**
     * 桩编号 (7字节)
     */
    @ProtocolField(order = 7, length = 7, dataType = ProtocolDataType.HEX, description = "桩编号")
    private String stationNumber;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer gunNumber;

    /**
     * 开始时间 (7字节 CP56Time2a格式)
     */
    @ProtocolField(order = 9, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "开始时间")
    private String startTime;

    /**
     * 结束时间 (7字节 CP56Time2a格式)
     */
    @ProtocolField(order = 10, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "结束时间")
    private String endTime;

    // 尖时段数据
    /**
     * 尖单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 11,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "尖单价")
    private BigDecimal peakPrice;

    /**
     * 尖电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 12,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "尖电量")
    private BigDecimal peakElectricity;

    /**
     * 尖损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 13,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "尖损电量")
    private BigDecimal peakLossElectricity;

    /**
     * 尖金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 14,
            length = 4,
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
            order = 15,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "峰单价")
    private BigDecimal highPrice;

    /**
     * 峰电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 16,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "峰电量")
    private BigDecimal highElectricity;

    /**
     * 峰损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 17,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "峰损电量")
    private BigDecimal highLossElectricity;

    /**
     * 峰金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 18,
            length = 4,
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
            order = 19,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "平单价")
    private BigDecimal normalPrice;

    /**
     * 平电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 20,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "平电量")
    private BigDecimal normalElectricity;

    /**
     * 平损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 21,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "平损电量")
    private BigDecimal normalLossElectricity;

    /**
     * 平金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 22,
            length = 4,
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
            order = 23,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "谷单价")
    private BigDecimal valleyPrice;

    /**
     * 谷电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 24,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷电量")
    private BigDecimal valleyElectricity;

    /**
     * 谷损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 25,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷损电量")
    private BigDecimal valleyLossElectricity;

    /**
     * 谷金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 26,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "谷金额")
    private BigDecimal valleyAmount;

    // 深谷时段数据
    /**
     * 深谷单价 (4字节，5位小数)
     */
    @ProtocolField(
            order = 27,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 5,
            description = "深谷单价")
    private BigDecimal deepValleyPrice;

    /**
     * 深谷电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 28,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "深谷电量")
    private BigDecimal deepValleyElectricity;

    /**
     * 深谷损电量 (4字节，4位小数)
     */
    @ProtocolField(
            order = 29,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "深谷损电量")
    private BigDecimal deepValleyLossElectricity;

    /**
     * 深谷金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 30,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "深谷金额")
    private BigDecimal deepValleyAmount;

    /**
     * 预留字段 (18字节)
     */
    @ProtocolField(order = 31, length = 18, dataType = ProtocolDataType.HEX, description = "预留字段")
    private String reserved;

    /**
     * 消费金额 (4字节，4位小数)
     */
    @ProtocolField(
            order = 32,
            length = 4,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            precision = 4,
            description = "消费金额")
    private BigDecimal totalAmount;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 33, length = 17, dataType = ProtocolDataType.ASCII, description = "帧类型")
    private String VIN;

    /**
     * 校验码 (2字节)
     */
    @ProtocolField(order = 34, length = 1, dataType = ProtocolDataType.HEX, littleEndian = true, description = "校验码")
    private String checksum;

    //    /**
    //     * 校验码 (2字节)
    //     */
    //    @ProtocolField(order = 34, length = 2, dataType = ProtocolDataType.HEX, littleEndian = true, description =
    // "校验码")
    //    private String checksum;
}
