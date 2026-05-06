package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 卡数据同步应答
 * 对应协议帧类型 0x43（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x43", name = "卡数据同步应答", description = "离线卡数据同步应答", version = "1.6")
public class YkcV16OfflineCardSyncReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 同步结果 (1字节)
     * BIN码: 0x00 失败, 0x01 成功
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "保存结果")
    private Integer syncResult;

    /**
     * 失败原因 (1字节)
     * BIN码: 0x01 卡号格式错误, 0x02 储存空间不足
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
