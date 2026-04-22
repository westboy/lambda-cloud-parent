package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YkcV20OfflineCardSyncCard {

    @ProtocolField(order = 1, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNo;

    @ProtocolField(order = 2, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;
}
