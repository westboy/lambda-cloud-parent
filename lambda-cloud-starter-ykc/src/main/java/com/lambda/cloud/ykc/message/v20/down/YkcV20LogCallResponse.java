package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "97", name = "日志召唤应答", description = "桩本地日志召唤应答")
public class YkcV20LogCallResponse {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "召唤结果")
    private Integer callResult;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;

    @ProtocolField(
            order = 4,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "日志文件数量")
    private Integer logFileCount;

    @ProtocolField(
            order = 5,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "日志文件编码")
    private Integer logFileEncoding;
}
