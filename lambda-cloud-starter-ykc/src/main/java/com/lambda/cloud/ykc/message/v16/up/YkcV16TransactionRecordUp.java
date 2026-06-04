package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议交易记录消息体
 * <p>
 * 对应协议帧类型 0x3B，充电桩在网络正常情况下，主动发送结算账单
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x3B", name = "交易记录", description = "充电桩在网络正常情况下，主动发送结算账单", version = "1.6")
public class YkcV16TransactionRecordUp {

    /** 交易流水号 (16字节) BCD码 */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /** 桩编号 (7字节) BCD码，不足7位补0 */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BCD码 */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 开始时间 (7字节) CP56Time2a格式 */
    @ProtocolField(
            order = 4,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "开始时间")
    private LocalDateTime startTime;

    /** 结束时间 (7字节) CP56Time2a格式 */
    @ProtocolField(
            order = 5,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "结束时间")
    private LocalDateTime endTime;

    /** 尖单价 (4字节) 精确到小数点后五位 */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖单价")
    private BigDecimal peakPrice;

    /** 尖电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 7,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "尖电量")
    private BigDecimal peakEnergy;

    /** 计损尖电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 8,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损尖电量")
    private BigDecimal peakEnergyWithLoss;

    /** 尖金额 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 9,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "尖金额")
    private BigDecimal peakAmount;

    /** 峰单价 (4字节) 精确到小数点后五位 */
    @ProtocolField(
            order = 10,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "峰单价")
    private BigDecimal highPrice;

    /** 峰电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 11,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "峰电量")
    private BigDecimal highEnergy;

    /** 计损峰电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 12,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损峰电量")
    private BigDecimal highEnergyWithLoss;

    /** 峰金额 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 13,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "峰金额")
    private BigDecimal highAmount;

    /** 平单价 (4字节) 精确到小数点后五位 */
    @ProtocolField(
            order = 14,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平单价")
    private BigDecimal normalPrice;

    /** 平电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 15,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "平电量")
    private BigDecimal normalEnergy;

    /** 计损平电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 16,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损平电量")
    private BigDecimal normalEnergyWithLoss;

    /** 平金额 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 17,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "平金额")
    private BigDecimal normalAmount;

    /** 谷单价 (4字节) 精确到小数点后五位 */
    @ProtocolField(
            order = 18,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷单价")
    private BigDecimal valleyPrice;

    /** 谷电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 19,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "谷电量")
    private BigDecimal valleyEnergy;

    /** 计损谷电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 20,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损谷电量")
    private BigDecimal valleyEnergyWithLoss;

    /** 谷金额 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 21,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "谷金额")
    private BigDecimal valleyAmount;

    /** 电表总起值 (5字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 22,
            length = 5,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "电表总起值")
    private String meterStartValue;

    /** 电表总止值 (5字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 23,
            length = 5,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "电表总止值")
    private String meterEndValue;

    /** 总电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 24,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "总电量")
    private BigDecimal totalEnergy;

    /** 计损总电量 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 25,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损总电量")
    private BigDecimal totalEnergyWithLoss;

    /** 消费金额 (4字节) 精确到小数点后四位 */
    @ProtocolField(
            order = 26,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "消费金额")
    private BigDecimal totalAmount;

    /** 电动汽车唯一标识 (17字节) ASCII VIN码，正序上传 */
    @ProtocolField(
            order = 27,
            length = 17,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            description = "电动汽车唯一标识")
    private String vinCode;

    /** 交易标识 (1字节) BIN码 */
    @ProtocolField(order = 28, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "交易标识")
    private Integer transactionType;

    /** 交易日期、时间 (7字节) CP56Time2a格式 */
    @ProtocolField(
            order = 29,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "交易日期、时间")
    private LocalDateTime transactionDateTime;

    /** 停止原因 (1字节) BIN码 */
    @ProtocolField(order = 30, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "停止原因")
    private Integer stopReason;

    /** 物理卡号 (8字节) BIN码，不足8位补0 */
    @ProtocolField(order = 31, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;
}
