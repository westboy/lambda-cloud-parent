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
@ProtocolPayload(frameType = "60", name = "默认最大功率下发", description = "运营平台向桩下发默认最大功率限制")
public class YkcV20DefaultMaxPowerRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(
            order = 3,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "默认最大功率")
    private Integer defaultMaxPower;

    @ProtocolField(order = 4, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "开始时间")
    private LocalDateTime startTime;

    @ProtocolField(order = 5, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "结束时间")
    private LocalDateTime endTime;
}
