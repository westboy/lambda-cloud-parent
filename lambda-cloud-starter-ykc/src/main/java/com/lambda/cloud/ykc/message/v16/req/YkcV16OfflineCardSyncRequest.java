package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v16.model.YkcV16OfflineCardSyncCard;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x44", name = "离线卡数据同步", description = "平台下发离线卡数据到充电桩", version = "1.6")
public class YkcV16OfflineCardSyncRequest {
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "下发卡个数")
    private Integer cardCount;

    @ProtocolField(
            order = 3,
            length = 16,
            computed = true,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV16OfflineCardSyncCard.class,
            listElementSizeField = "cardCount",
            description = "离线卡列表")
    private List<YkcV16OfflineCardSyncCard> cards;
}
