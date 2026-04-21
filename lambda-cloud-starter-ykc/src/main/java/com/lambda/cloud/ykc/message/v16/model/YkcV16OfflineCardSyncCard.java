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
@ProtocolPayload(name = "离线卡同步卡信息", description = "离线卡逻辑卡号+物理卡号")
public class YkcV16OfflineCardSyncCard {
    @ProtocolField(order = 1, length = 8, computed = true, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNo;

    @ProtocolField(order = 2, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;
}
