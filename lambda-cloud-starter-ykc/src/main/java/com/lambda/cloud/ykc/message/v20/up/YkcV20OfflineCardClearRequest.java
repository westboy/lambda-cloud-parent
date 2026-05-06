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
 * 云快充2.0协议 - 离线卡数据清除详细信息
 *
 * <p>帧类型码：0x46</p>
 * <p>对应协议文档：8.13 离线卡数据清除</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "46", name = "离线卡数据清除", description = "平台下发离线卡数据清除到充电桩")
public class YkcV20OfflineCardClearRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "清除卡个数")
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
