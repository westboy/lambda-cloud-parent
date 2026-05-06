package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 参数配置阶段（BMS/桩能力）
 * 对应协议帧类型 0x17（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x17", name = "参数配置", description = "参数配置阶段，BMS/桩能力参数上送", version = "1.6")
public class YkcV16ParameterConfigUp {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** BMS 单体最高允许充电电压 (2字节) 0.01V */
    @ProtocolField(
            order = 4,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "单体最高允许充电电压V")
    private Integer cellMaxAllowChargeVoltage;

    /** BMS 最高允许充电电流 (2字节) 0.1A */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "最高允许充电电流A")
    private Integer bmsMaxAllowChargeCurrent;

    /** BMS 标称总能量 (2字节) 0.1kWh */
    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "标称总能量kWh")
    private Integer bmsNominalTotalEnergy;

    /** BMS 最高允许充电总电压 (2字节) 0.1V */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "最高允许充电总电压V")
    private Integer bmsMaxAllowChargeVoltage;

    /** BMS 最高允许温度 (1字节) -50℃偏移 */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高允许温度℃")
    private Integer bmsMaxAllowTemperature;

    /** BMS 整车动力蓄电池荷电状态SOC (2字节) 0.1% */
    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "荷电状态SOC%")
    private Integer bmsSoc;

    /** BMS 整车动力蓄电池当前电池电压 (2字节) 0.1V */
    @ProtocolField(
            order = 10,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "当前电池电压V")
    private Integer bmsCurrentBatteryVoltage;

    /** 电桩最高输出电压 (2字节) 0.1V */
    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩最高输出电压V")
    private Integer chargerMaxOutputVoltage;

    /** 电桩最低输出电压 (2字节) 0.1V */
    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩最低输出电压V")
    private Integer chargerMinOutputVoltage;

    /** 电桩最大输出电流 (2字节) 0.1A */
    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩最大输出电流A")
    private Integer chargerMaxOutputCurrent;

    /** 电桩最小输出电流 (2字节) 0.1A */
    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩最小输出电流A")
    private Integer chargerMinOutputCurrent;
}
