package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardSyncCard;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡数据同步详细信息
 *
 * <p>帧类型码：0x44</p>
 * <p>对应协议文档：8.11 离线卡数据同步</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "44", name = "离线卡数据同步", description = "平台下发离线卡数据到充电桩")
public class YkcV20OfflineCardSyncRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "下发卡个数")
    private Integer cardCount;

    @ProtocolField(
            order = 3,
            length = 16,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20OfflineCardSyncCard.class,
            listElementSizeField = "cardCount",
            description = "离线卡列表")
    private List<YkcV20OfflineCardSyncCard> cards;
}
