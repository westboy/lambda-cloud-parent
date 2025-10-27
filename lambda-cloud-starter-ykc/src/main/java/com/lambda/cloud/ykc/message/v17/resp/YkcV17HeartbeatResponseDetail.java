package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议心跳响应消息体
 * <p>
 * 对应协议帧类型 0x04，心跳包应答的消息体部分
 * 样例报文: 68 0D 0000 00 04 32010200000001 01 00 0069
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x04", name = "心跳响应", description = "心跳包应答消息体", version = "1.7")
public class YkcV17HeartbeatResponseDetail {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 枪状态 (1字节)
     * BIN码: 0x00 正常, 0x01 故障
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪状态")
    private Integer connectorStatus;
}
