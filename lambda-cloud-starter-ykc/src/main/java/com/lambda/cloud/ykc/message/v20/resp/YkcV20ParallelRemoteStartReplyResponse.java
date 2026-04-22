package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A3", name = "远程并充启机命令回复", description = "充电桩对并充远程启机命令的回复")
public class YkcV20ParallelRemoteStartReplyResponse {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "启动结果")
    private Integer startResult;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;

    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "主辅枪标记")
    private Integer mainOrSubGunFlag;

    @ProtocolField(order = 7, length = 6, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}
