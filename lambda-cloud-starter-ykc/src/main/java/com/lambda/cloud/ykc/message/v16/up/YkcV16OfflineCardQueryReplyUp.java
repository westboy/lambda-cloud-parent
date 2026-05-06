package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v16.model.YkcV16OfflineCardQueryResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 离线卡数据查询应答
 * 对应协议帧类型 0x47（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x47", name = "离线卡数据查询应答", description = "离线卡数据查询应答", version = "1.6")
public class YkcV16OfflineCardQueryReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(
            order = 2,
            length = 9,
            computed = true,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV16OfflineCardQueryResult.class,
            description = "离线卡查询结果列表")
    private List<YkcV16OfflineCardQueryResult> results;
}
