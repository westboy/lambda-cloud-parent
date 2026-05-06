package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 远程更新应答
 * 对应协议帧类型 0x93（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x93", name = "远程更新应答", description = "远程更新应答", version = "1.6")
public class YkcV16RemoteUpdateReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 更新结果 (1字节)
     * BIN码: 0x00 成功, 0x01 失败
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "更新结果")
    private Integer updateResult;
}
