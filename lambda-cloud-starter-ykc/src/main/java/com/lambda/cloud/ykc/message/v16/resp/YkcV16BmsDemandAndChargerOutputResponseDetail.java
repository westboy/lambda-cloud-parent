package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 BMS需求与充电机输出
 * 对应协议帧类型 0x23
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x23", name = "BMS需求与充电机输出", description = "BMS需求与充电机输出信息上送", version = "1.6")
public class YkcV16BmsDemandAndChargerOutputResponseDetail {

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

    /** BMS 充电模式 (1字节) */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS充电模式")
    private Integer bmsChargingMode;

    /** BMS 实测电压 (2字节) 0.1V */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS实测电压V")
    private Integer bmsMeasuredVoltage;

    /** BMS 实测电流 (2字节) 0.1A */
    @ProtocolField(
            order = 8,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS实测电流A")
    private Integer bmsMeasuredCurrent;

    /** BMS 单体最高电压及序号 (2字节) 低12位电压0.01V，高4位序号 */
    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "单体最高电压及序号(合并域)")
    private Integer bmsHighestCellVoltageWithIndex;

    /** BMS SOC (1字节) */
    @ProtocolField(order = 10, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS SOC%")
    private Integer bmsSoc;

    /** 估算剩余充电时间 (2字节) min */
    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "估算剩余充电时间min")
    private Integer estimatedRemainingTime;

    /** 充电机输出电压 (2字节) 0.1V */
    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电机输出电压V")
    private Integer chargerOutputVoltage;

    /** 充电机输出电流 (2字节) 0.1A */
    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电机输出电流A")
    private Integer chargerOutputCurrent;

    /** 累计充电时间 (2字节) min */
    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "累计充电时间min")
    private Integer accumulatedChargingTime;
}
