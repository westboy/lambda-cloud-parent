package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议心跳请求消息体
 * <p>
 * 对应协议帧类型 0x03，充电桩心跳包
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x03", name = "心跳请求", description = "充电桩心跳包消息体", version = "1.6")
public class YkcV16HeartbeatUp {

    /**
     * 桩编码 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

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
