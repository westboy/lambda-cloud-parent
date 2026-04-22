package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20OfflineCardQueryResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "47", name = "离线卡数据查询应答", description = "离线卡查询结果")
public class YkcV20OfflineCardQueryResponse {
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(
            order = 2,
            length = 9,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20OfflineCardQueryResult.class,
            description = "查询结果列表")
    private List<YkcV20OfflineCardQueryResult> results;
}
