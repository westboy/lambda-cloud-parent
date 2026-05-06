package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 9.4 对时设置应答
 * 对应协议帧类型 0x55（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x55", name = "对时设置应答", description = "充电桩对时设置应答（CP56Time2a）", version = "1.6")
public class YkcV16TimeSyncUp {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 当前时间 (7字节) CP56Time2a */
    @ProtocolField(
            order = 2,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "当前时间")
    private byte[] currentTime;
}
