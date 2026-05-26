package com.lambda.cloud.t645.message.heartbeat;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 4G/NB 心跳上报请求报文。
 *
 * <p>控制码 {@code 0x00}，无标准 DI。由智能表计通过 4G/NB 网络主动上报，
 * 用于维持与服务器的长连接状态。数据域不做 +0x33 偏移处理。</p>
 */
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

    /** 心跳报文无标准 DI，返回 "NONE"。 */
    @Override
    public String getDi() {
        return "NONE"; // 心跳无标准DI
    }
}
