package com.lambda.cloud.ykc.message.v16.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@ProtocolPayload(name = "离线卡清除结果", description = "离线卡清除结果条目")
public class YkcV16OfflineCardClearResult {
    @ProtocolField(order = 1, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;

    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "清除标记")
    private Integer clearFlag;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}
