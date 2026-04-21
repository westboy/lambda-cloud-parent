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
@ProtocolPayload(frameType = "5F", name = "参数设置", description = "平台下发参数配置信息到桩")
public class YkcV20WorkingParamsSetRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "即插即充开关")
    private Integer plugAndChargeSwitch;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "鉴权超时时间")
    private Integer authTimeoutSeconds;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "离线充电时间")
    private Integer offlineChargeSeconds;
}

