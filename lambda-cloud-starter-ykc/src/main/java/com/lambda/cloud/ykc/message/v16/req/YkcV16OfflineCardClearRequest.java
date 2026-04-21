package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x46", name = "离线卡数据清除", description = "平台下发离线卡清除请求", version = "1.6")
public class YkcV16OfflineCardClearRequest {
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "清除离线卡的个数")
    private Integer cardCount;

    @ProtocolField(
            order = 3,
            length = 8,
            computed = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.HEX,
            listElementSizeField = "cardCount",
            description = "物理卡号列表")
    private List<String> physicalCardNos;
}
