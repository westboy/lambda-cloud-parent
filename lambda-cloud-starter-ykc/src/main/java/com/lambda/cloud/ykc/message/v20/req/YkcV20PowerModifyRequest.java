package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "52", name = "功率修改", description = "运营平台下发充电桩最大功率限制")
public class YkcV20PowerModifyRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "允许最大功率")
    private Integer maxPower;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "指令响应优先级")
    private Integer priority;

    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "限制时间")
    private Integer limitTime;
}

