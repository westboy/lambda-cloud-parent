package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 充电结束阶段信息
 * 对应协议帧类型 0x19
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x19", name = "充电结束", description = "充电结束阶段信息上送", version = "1.6")
public class YkcV16ChargingEndResponse {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** SOC (1字节) */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "SOC%")
    private Integer soc;

    /** 单体最低电压 (2字节) 0.01V */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "单体最低电压V")
    private Integer minCellVoltage;

    /** 单体最高电压 (2字节) 0.01V */
    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 2,
            description = "单体最高电压V")
    private Integer maxCellVoltage;

    /** 最低温度 (1字节) */
    @ProtocolField(order = 7, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最低温度℃")
    private Integer minTemperature;

    /** 最高温度 (1字节) */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高温度℃")
    private Integer maxTemperature;

    /** 累计充电时间 (2字节) min */
    @ProtocolField(
            order = 9,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "累计充电时间min")
    private Integer totalChargingTime;

    /** 输出能量 (2字节) 0.1kWh */
    @ProtocolField(
            order = 10,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "输出能量kWh")
    private Integer outputEnergy;

    /** 充电机编号 (4字节) BIN */
    @ProtocolField(order = 11, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "充电机编号")
    private String chargerCode;
}
