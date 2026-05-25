package com.lambda.cloud.t645.message.heartbeat;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "80:NONE", name = "4G/NB 心跳应答")
public class T645HeartbeatResponse implements T645DataBody {

    @Override
    public String getDi() {
        return "NONE";
    }
}
