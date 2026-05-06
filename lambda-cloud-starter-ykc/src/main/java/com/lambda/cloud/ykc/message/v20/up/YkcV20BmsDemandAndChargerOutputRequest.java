package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "23", name = "充电过程BMS需求、充电机输出", description = "BMS需求与充电机输出信息上送", version = "2.0")
public class YkcV20BmsDemandAndChargerOutputRequest {
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(
            order = 4,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS电压需求V")
    private Integer bmsVoltageDemand;

    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS电流需求A")
    private Integer bmsCurrentDemand;

    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS充电模式")
    private Integer bmsChargingMode;

    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS实测电压V")
    private Integer bmsMeasuredVoltage;

    @ProtocolField(
            order = 8,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS实测电流A")
    private Integer bmsMeasuredCurrent;

    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "单体最高电压及序号(合并域)")
    private Integer bmsHighestCellVoltageWithIndex;

    @ProtocolField(order = 10, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS SOC%")
    private Integer bmsSoc;

    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "估算剩余充电时间min")
    private Integer estimatedRemainingTime;

    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电机输出电压V")
    private Integer chargerOutputVoltage;

    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电机输出电流A")
    private Integer chargerOutputCurrent;

    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "累计充电时间min")
    private Integer accumulatedChargingTime;
}
