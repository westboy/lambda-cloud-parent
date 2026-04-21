package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录召唤确认详细信息
 *
 * <p>帧类型码：0x4C</p>
 * <p>对应协议文档：8.24 交易记录召唤确认</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4C", name = "交易记录召唤确认", description = "桩对交易记录召唤的回复确认")
public class YkcV20TransactionRecordCallResponse {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "召唤结果")
    private Integer callResult;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}

