package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 11.1 远程重启（下行）
 * 对应协议帧类型 0x92
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x92", name = "远程重启", description = "远程重启充电桩，应对卡死等问题", version = "1.6")
public class YkcV16RemoteRebootRequest {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 执行控制 (1字节) BIN码：0x01 立即执行；0x02 空闲执行 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executeControl;
}
