package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡数据清除
 * <p>
 * 帧类型码：0x46
 * 对应协议文档：8.13 离线卡数据清除
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：离线卡清除是平台主动下发的操作，平台在充电桩在线时会下发此数据帧到充电桩
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "46", name = "离线卡数据清除", description = "离线卡数据清除命令")
public class YkcV20OfflineCardClearDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 清除类型 (1字节)
     * BIN码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "清除类型")
    private Integer clearType;
}
