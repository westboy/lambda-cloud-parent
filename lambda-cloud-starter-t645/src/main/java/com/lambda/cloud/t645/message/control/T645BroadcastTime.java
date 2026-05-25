package com.lambda.cloud.t645.message.control;

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
@ProtocolPayload(frameType = "08:BROADCAST_TIME", name = "广播校时")
public class T645BroadcastTime implements T645DataBody {

    @ProtocolField(order = 0, length = 6, dataType = ProtocolDataType.CP56TIME2A, computed = true, description = "校时时间")
    private String broadcastTime;

    @Override
    public String getDi() {
        return null;
    }
}
