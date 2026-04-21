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
@ProtocolPayload(frameType = "5D", name = "平台连接设置", description = "平台下发连接信息到桩，桩根据下发参数修改配置")
public class YkcV20PlatformConnectConfigRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 64, dataType = ProtocolDataType.ASCII, description = "平台域名")
    private String platformDomain;

    @ProtocolField(order = 3, length = 15, dataType = ProtocolDataType.ASCII, description = "平台地址")
    private String platformAddress;

    @ProtocolField(order = 4, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "平台端口")
    private Integer platformPort;
}

