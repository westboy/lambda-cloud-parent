package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardSyncCard;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.1.0协议 - 离线卡数据清除
 * <p>
 * 帧类型码：0x46
 * 对应协议文档：8.13 离线卡数据清除
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：离线卡清除是平台主动下发的操作
 * </p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "46", name = "离线卡数据清除", description = "离线卡数据清除命令")
public class YkcV20OfflineCardClearDown {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "清除卡个数")
    private Integer clearCardCount;

    @ProtocolField(
            order = 3,
            length = 16,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20OfflineCardSyncCard.class,
            listElementSizeField = "clearCardCount",
            description = "卡片列表")
    private List<YkcV20OfflineCardSyncCard> cards;
}
