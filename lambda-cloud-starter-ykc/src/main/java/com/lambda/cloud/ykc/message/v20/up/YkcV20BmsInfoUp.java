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
@ProtocolPayload(frameType = "25", name = "充电过程BMS信息", description = "BMS信息上送", version = "2.0")
public class YkcV20BmsInfoUp {
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高电压电池序号")
    private Integer highestCellVoltageIndex;

    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池最高温度℃")
    private Integer highestBatteryTemperature;

    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最高温度检测点编号")
    private Integer highestTemperaturePointIndex;

    @ProtocolField(order = 7, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池最低温度℃")
    private Integer lowestBatteryTemperature;

    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "最低温度检测点编号")
    private Integer lowestTemperaturePointIndex;

    @ProtocolField(
            order = 9,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "状态9-12合并字节")
    private Integer group0912;

    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "状态13-16合并字节")
    private Integer group1316;
}
