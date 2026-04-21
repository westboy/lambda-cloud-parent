package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v16.model.YkcV16OfflineCardClearResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x45", name = "离线卡数据清除应答", description = "离线卡清除结果", version = "1.6")
public class YkcV16OfflineCardClearResponse {
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(
            order = 2,
            length = 10,
            computed = true,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV16OfflineCardClearResult.class,
            description = "清除结果列表")
    private List<YkcV16OfflineCardClearResult> results;
}
