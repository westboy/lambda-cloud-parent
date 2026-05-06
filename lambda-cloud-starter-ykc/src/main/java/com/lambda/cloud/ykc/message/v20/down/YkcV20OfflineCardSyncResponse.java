package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡数据同步应答详细信息
 *
 * <p>帧类型码：0x43</p>
 * <p>对应协议文档：8.12 离线卡数据同步应答</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "43", name = "离线卡数据同步应答", description = "离线卡数据同步应答")
public class YkcV20OfflineCardSyncResponse {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "保存结果")
    private Integer saveResult;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}
