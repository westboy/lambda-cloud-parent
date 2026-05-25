package com.lambda.cloud.t645.message.heartbeat;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "00:NONE", name = "4G/NB 心跳上报")
public class T645HeartbeatRequest implements T645DataBody {

    @ProtocolField(
            order = 0,
            length = 1,
            dataType = ProtocolDataType.UINT8,
            description = "子功能码 (01表示带信号强度)",
            optional = true)
    private Integer subFunctionCode;

    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.UINT8, description = "信号强度", optional = true)
    private Integer signalStrength;

    @Override
    public String getDi() {
        return "NONE"; // 心跳无标准DI
    }
}
