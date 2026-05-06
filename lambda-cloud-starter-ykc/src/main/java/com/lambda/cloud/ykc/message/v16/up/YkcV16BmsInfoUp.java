package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 BMS信息
 * 对应协议帧类型 0x25（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x25", name = "BMS信息", description = "BMS信息上送", version = "1.6")
public class YkcV16BmsInfoUp {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 单体最高电压电池序号 (1字节) */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高电压电池序号")
    private Integer highestCellVoltageIndex;

    /** 电池最高温度 (1字节) */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池最高温度℃")
    private Integer highestBatteryTemperature;

    /** 最高温度检测点编号 (1字节) */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高温度检测点编号")
    private Integer highestTemperaturePointIndex;

    /** 最低动力蓄电池温度 (1字节) -50℃偏移 */
    @ProtocolField(
            order = 7,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "最低动力蓄电池温度℃")
    private Integer lowestBatteryTemperature;

    /** 最低动力蓄电池温度检测点编号 (1字节) */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最低温度检测点编号")
    private Integer lowestTemperaturePointIndex;

    /** BMS单体动力蓄电池电压过高/过低 (2位) */
    @ProtocolField(order = 9, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "单体电压过高/过低")
    private Integer cellVoltageStatus;

    /** BMS整车动力蓄电池荷电状态SOC过高/过低 (2位) */
    @ProtocolField(order = 10, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "SOC过高/过低")
    private Integer socStatus;

    /** BMS动力蓄电池充电过电流 (2位) */
    @ProtocolField(order = 11, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "充电过电流")
    private Integer chargeOverCurrentStatus;

    /** BMS动力蓄电池温度过高 (2位) */
    @ProtocolField(order = 12, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "温度过高")
    private Integer temperatureOverStatus;

    /** BMS动力蓄电池绝缘状态 (2位) */
    @ProtocolField(order = 13, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "绝缘状态")
    private Integer insulationStatus;

    /** BMS动力蓄电池组输出连接器连接状态 (2位) */
    @ProtocolField(
            order = 14,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "输出连接器连接状态")
    private Integer connectorStatus;

    /** 充电禁止 (2位) */
    @ProtocolField(order = 15, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "充电禁止")
    private Integer chargingProhibition;

    /** 预留位 (1字节) */
    @ProtocolField(order = 16, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved;
}
