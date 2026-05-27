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
 * 云快充V2.1.0协议 - 离线卡数据同步
 * <p>
 * 帧类型码：0x44
 * 对应协议文档：8.11 离线卡数据同步
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：离线卡适用于桩离线充电模式，平台在充电桩在线时会下发此数据帧到充电桩
 * </p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "44", name = "离线卡数据同步", description = "离线卡数据同步命令")
public class YkcV20OfflineCardSyncDown {

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
            description = "卡片列表")
    private List<YkcV20OfflineCardSyncCard> cards;
}
