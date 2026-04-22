package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(name = "离线卡查询结果")
public class YkcV20OfflineCardQueryResult {
    @ProtocolField(order = 1, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "查询结果")
    private Integer queryResult;
}
