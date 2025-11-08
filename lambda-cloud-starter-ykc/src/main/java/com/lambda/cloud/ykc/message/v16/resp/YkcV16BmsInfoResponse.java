package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 BMS信息
 * 对应协议帧类型 0x25
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x25", name = "BMS信息", description = "BMS信息上送", version = "1.6")
public class YkcV16BmsInfoResponse {

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

    /** 电池最低温度 (1字节) */
    @ProtocolField(order = 7, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池最低温度℃")
    private Integer lowestBatteryTemperature;

    /** 最低温度检测点编号 (1字节) */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最低温度检测点编号")
    private Integer lowestTemperaturePointIndex;

    /** BMS 状态（9-12 合并字节） */
    @ProtocolField(
            order = 9,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "状态9-12合并字节")
    private Integer group0912;

    /** BMS 状态（13-16 合并字节） */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "状态13-16合并字节")
    private Integer group1316;
}
