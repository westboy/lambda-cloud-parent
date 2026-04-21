package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A4", name = "运营平台远程控制并充启机", description = "并充模式远程启机命令下发")
public class YkcV20ParallelRemoteStartRequest {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    @ProtocolField(order = 5, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    @ProtocolField(order = 6, length = 4, dataType = ProtocolDataType.UINT32, littleEndian = true, description = "账户余额")
    private Long accountBalance;

    @ProtocolField(order = 7, length = 6, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}

