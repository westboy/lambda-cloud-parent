package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩心跳包请求详细信息
 * <p>
 * 帧类型码：0x03
 * 对应协议文档：6.3 充电桩心跳包
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "03", name = "充电桩心跳包", description = "充电桩心跳包请求详细信息")
public class YkcV20HeartbeatRequestDetail {

    /**
     * 桩编码 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 枪状态 (1字节)
     * 0x00：正常
     * 0x01：故障
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "枪状态")
    private Integer connectorStatus;
}
