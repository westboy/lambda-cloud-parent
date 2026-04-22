package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "48", name = "离线卡数据查询", description = "平台下发离线卡查询请求")
public class YkcV20OfflineCardQueryRequest {
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "查询的离线卡个数")
    private Integer cardCount;

    @ProtocolField(
            order = 3,
            length = 8,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.HEX,
            listElementSizeField = "cardCount",
            description = "物理卡号列表")
    private List<String> physicalCardNos;
}
