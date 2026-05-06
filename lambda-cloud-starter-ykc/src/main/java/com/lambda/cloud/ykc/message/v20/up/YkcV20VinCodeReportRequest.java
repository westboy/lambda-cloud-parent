package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A9", name = "充电桩上报vin码", description = "充电桩上报VIN码")
public class YkcV20VinCodeReportRequest {
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vinCode;

    @ProtocolField(order = 4, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "当前时间")
    private LocalDateTime currentTime;
}
