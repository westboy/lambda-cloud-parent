package com.lambda.cloud.netty.protocol.list;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ProtocolPayload(name = "CompositeListItem")
public class CompositeListItem {
    @ProtocolField(order = 1, length = 1, computed = true, dataType = ProtocolDataType.UINT8)
    private Integer a;

    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8)
    private Integer b;
}
