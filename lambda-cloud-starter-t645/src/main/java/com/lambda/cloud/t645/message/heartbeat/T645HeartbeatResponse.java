package com.lambda.cloud.t645.message.heartbeat;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 4G/NB 心跳应答报文。
 *
 * <p>控制码 {@code 0x80}，无标准 DI。服务器收到心跳上报后返回此应答，
 * 告知表计连接状态正常。数据域不做 +0x33 偏移处理。</p>
 */
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
