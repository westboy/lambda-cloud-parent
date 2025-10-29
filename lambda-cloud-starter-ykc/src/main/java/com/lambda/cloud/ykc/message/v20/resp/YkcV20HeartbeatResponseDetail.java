package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 心跳包应答响应详细信息
 * <p>
 * 帧类型码：0x04
 * 对应协议文档：6.4 心跳包应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "04", name = "心跳包应答", description = "心跳包应答详细信息")
public class YkcV20HeartbeatResponseDetail {

    /**
     * 桩编码 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 心跳应答 (1字节)
     * 置0
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "心跳应答")
    private Integer heartbeatResponse;
}
