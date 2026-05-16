package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A6", name = "运营平台确认启动充电", description = "运营平台对启动充电申请的确认回复")
public class YkcV20StartChargingDown {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    @ProtocolField(
            order = 5,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 2,
            description = "账户余额")
    private Integer accountBalance;

    @ProtocolField(order = 6, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "最大功率")
    private Integer maxPower;

    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "SOC限制")
    private Integer socLimit;

    @ProtocolField(
            order = 8,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 2,
            description = "充电电量限制")
    private Integer chargingEnergyLimit;

    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "鉴权成功标志")
    private Integer authSuccessFlag;

    @ProtocolField(order = 10, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}
