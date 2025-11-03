package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.0协议参数配置响应详细信息
 * <p>
 * 对应协议帧类型 0x17，GBT-27930充电桩与BMS参数配置阶段报文
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "17", name = "参数配置", description = "GBT-27930充电桩与BMS参数配置阶段报文", version = "2.0")
public class YkcV20BmsConfigRequestDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码，见名词解释
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * BMS单体动力蓄电池最高允许充电电压 (2字节)
     * BIN码，0.01V/位，0V偏移量；数据范围：0~24V
     */
    @ProtocolField(
            order = 4,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS单体动力蓄电池最高允许充电电压")
    private Integer bmsMaxCellVoltage;

    /**
     * BMS最高允许充电电流 (2字节)
     * BIN码，0.1A/位，-400A偏移量
     */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS最高允许充电电流")
    private Integer bmsMaxChargingCurrent;

    /**
     * BMS动力蓄电池标称总能量 (2字节)
     * BIN码，0.1kWh/位，0kWh偏移量
     */
    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS动力蓄电池标称总能量")
    private Integer bmsNominalTotalEnergy;

    /**
     * BMS最高允许充电总电压 (2字节)
     * BIN码，0.1V/位，0V偏移量
     */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS最高允许充电总电压")
    private Integer bmsMaxTotalVoltage;

    /**
     * BMS最高允许温度 (1字节)
     * BIN码，1℃/位，-50℃偏移量
     */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS最高允许温度")
    private Integer bmsMaxTemperature;

    /**
     * BMS整车动力蓄电池荷电状态SOC (2字节)
     * BIN码，0.1%/位，0%偏移量
     */
    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS整车动力蓄电池荷电状态SOC")
    private Integer bmsSoc;

    /**
     * BMS整车动力蓄电池当前电池电压 (2字节)
     * BIN码，0.1V/位，0V偏移量
     */
    @ProtocolField(
            order = 10,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS整车动力蓄电池当前电池电压")
    private Integer bmsCurrentVoltage;

    /**
     * 充电桩最高输出电压 (2字节)
     * BIN码，0.1V/位，0V偏移量
     */
    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "充电桩最高输出电压")
    private Integer chargerMaxOutputVoltage;

    /**
     * 充电桩最低输出电压 (2字节)
     * BIN码，0.1V/位，0V偏移量
     */
    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "充电桩最低输出电压")
    private Integer chargerMinOutputVoltage;

    /**
     * 充电桩最大输出电流 (2字节)
     * BIN码，0.1A/位，-400A偏移量
     */
    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "充电桩最大输出电流")
    private Integer chargerMaxOutputCurrent;

    /**
     * 充电桩最小输出电流 (2字节)
     * BIN码，0.1A/位，-400A偏移量
     */
    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "充电桩最小输出电流")
    private Integer chargerMinOutputCurrent;
}
