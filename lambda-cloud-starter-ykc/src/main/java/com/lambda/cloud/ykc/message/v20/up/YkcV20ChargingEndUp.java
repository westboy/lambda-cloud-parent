package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电结束
 * <p>
 * 帧类型码：0x19
 * 对应协议文档：7.5 充电结束
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：GBT-27930充电桩与BMS充电结束阶段报文
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "19", name = "充电结束", description = "GBT-27930充电桩与BMS充电结束阶段报文")
public class YkcV20ChargingEndUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * BMS中止荷电状态SOC (1字节)
     * 1%/位，0%偏移量；数据范围：0~100%
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS中止荷电状态SOC")
    private Integer bmsEndSoc;

    /**
     * BMS动力蓄电池单体最低电压 (2字节)
     * 0.01V/位，0V偏移量；数据范围：0~24V
     */
    @ProtocolField(
            order = 5,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "BMS动力蓄电池单体最低电压")
    private Integer bmsMinCellVoltage;

    /**
     * BMS动力蓄电池单体最高电压 (2字节)
     * 0.01V/位，0V偏移量；数据范围：0~24V
     */
    @ProtocolField(
            order = 6,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "BMS动力蓄电池单体最高电压")
    private Integer bmsMaxCellVoltage;

    /**
     * BMS动力蓄电池最低温度 (1字节)
     * 1ºC/位，-50ºC偏移量；数据范围：-50ºC~+200ºC
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS动力蓄电池最低温度")
    private Integer bmsMinTemperature;

    /**
     * BMS动力蓄电池最高温度 (1字节)
     * 1ºC/位，-50ºC偏移量；数据范围：-50ºC~+200ºC
     */
    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS动力蓄电池最高温度")
    private Integer bmsMaxTemperature;

    /**
     * 电桩累计充电时间 (2字节)
     * 1min/位，0min偏移量；数据范围：0~600min
     */
    @ProtocolField(
            order = 9,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "电桩累计充电时间")
    private Integer chargingDuration;

    /**
     * 电桩输出能量 (2字节)
     * 0.1kWh/位，0kWh偏移量；数据范围：0~1000kWh
     */
    @ProtocolField(
            order = 10,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "电桩输出能量")
    private Integer outputEnergy;

    /**
     * 充电机编号 (4字节)
     * BIN码，充电机编号，1/位，1偏移量
     */
    @ProtocolField(order = 11, length = 4, dataType = ProtocolDataType.HEX, description = "充电机编号")
    private String chargerId;
}
