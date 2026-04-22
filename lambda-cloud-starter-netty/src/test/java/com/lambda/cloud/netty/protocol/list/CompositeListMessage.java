package com.lambda.cloud.netty.protocol.list;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ProtocolPayload(frameType = "F1", name = "CompositeListMessage")
public class CompositeListMessage {
    @ProtocolField(order = 1, length = 1, computed = true, dataType = ProtocolDataType.UINT8)
    private Integer count;

    @ProtocolField(
            order = 2,
            length = 2,
            computed = true,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = CompositeListItem.class,
            listElementSizeField = "count")
    private List<CompositeListItem> items;
}
