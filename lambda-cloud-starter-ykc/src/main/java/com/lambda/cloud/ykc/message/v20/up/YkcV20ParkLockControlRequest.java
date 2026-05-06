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
@ProtocolPayload(frameType = "62", name = "遥控地锁升锁与降锁命令", description = "平台下发命令控制地锁升降")
public class YkcV20ParkLockControlRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "升/降地锁")
    private Integer action;

    @ProtocolField(order = 4, length = 4, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
