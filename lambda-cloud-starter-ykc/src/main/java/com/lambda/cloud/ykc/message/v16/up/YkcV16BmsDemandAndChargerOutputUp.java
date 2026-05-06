package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 BMS需求与充电机输出
 * 对应协议帧类型 0x23（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x23", name = "BMS需求与充电机输出", description = "BMS需求与充电机输出信息上送", version = "1.6")
public class YkcV16BmsDemandAndChargerOutputUp {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** BMS 电压需求 (2字节) 0.1V */
    @ProtocolField(
            order = 4,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS电压需求V")
    private Integer bmsVoltageDemand;

    /** BMS 电流需求 (2字节) 0.1A */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS电流需求A")
    private Integer bmsCurrentDemand;

    /** BMS 充电模式 (1字节) 0x01恒压充电；0x02恒流充电 */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS充电模式")
    private Integer bmsChargingMode;

    /** BMS充电电压测量值 (2字节) 0.1V */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS充电电压测量值V")
    private Integer bmsChargeVoltageMeasured;

    /** BMS充电电流测量值 (2字节) 0.1A，-400A偏移量 */
    @ProtocolField(
            order = 8,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS充电电流测量值A")
    private Integer bmsChargeCurrentMeasured;

    /** BMS最高单体动力蓄电池电压及组号 (2字节) */
    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "BMS最高单体电压及组号")
    private Integer bmsHighestCellVoltageAndGroup;

    /** BMS当前荷电状态SOC (1字节) 1%/位 */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "BMS当前SOC%")
    private Integer bmsSoc;

    /** BMS估算剩余充电时间 (2字节) 1min/位 */
    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "BMS估算剩余充电时间min")
    private Integer bmsEstimatedRemainingTime;

    /** 电桩电压输出值 (2字节) 0.1V */
    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩电压输出值V")
    private Integer chargerOutputVoltage;

    /** 电桩电流输出值 (2字节) 0.1A，-400A偏移量 */
    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩电流输出值A")
    private Integer chargerOutputCurrent;

    /** 累计充电时间 (2字节) 1min/位 */
    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "累计充电时间min")
    private Integer totalChargingTime;
}
