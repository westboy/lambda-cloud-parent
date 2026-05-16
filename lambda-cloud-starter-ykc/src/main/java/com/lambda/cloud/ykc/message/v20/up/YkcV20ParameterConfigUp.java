package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 参数配置
 * <p>
 * 帧类型码：0x17
 * 对应协议文档：7.4 参数配置
 * 数据传输方向：充电桩 → 运营平台（上行）
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "17", name = "参数配置", description = "GBT-27930充电桩与BMS参数配置阶段报文", version = "2.0")
public class YkcV20ParameterConfigUp {

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
            precision = 2,
            description = "BMS单体动力蓄电池最高允许充电电压")
    private Integer bmsMaxCellVoltage;

    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS最高允许充电电流")
    private Integer bmsMaxChargingCurrent;

    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS动力蓄电池标称总能量")
    private Integer bmsNominalTotalEnergy;

    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS最高允许充电总电压")
    private Integer bmsMaxTotalVoltage;

    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS最高允许温度")
    private Integer bmsMaxTemperature;

    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS整车动力蓄电池荷电状态SOC")
    private Integer bmsSoc;

    @ProtocolField(
            order = 10,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "BMS整车动力蓄电池当前电池电压")
    private Integer bmsCurrentVoltage;

    @ProtocolField(
            order = 11,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电桩最高输出电压")
    private Integer chargerMaxOutputVoltage;

    @ProtocolField(
            order = 12,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电桩最低输出电压")
    private Integer chargerMinOutputVoltage;

    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电桩最大输出电流")
    private Integer chargerMaxOutputCurrent;

    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "充电桩最小输出电流")
    private Integer chargerMinOutputCurrent;
}
