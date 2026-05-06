package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 远程停机命令回复
 * 对应协议帧类型 0x35（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x35", name = "远程停机命令回复", description = "远程停机命令回复", version = "1.6")
public class YkcV16RemoteStopReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 停止充电结果 (1字节)
     * BIN码: 0x00 失败, 0x01 成功
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "停止结果")
    private Integer stopResult;

    /**
     * 失败原因 (1字节)
     * BIN码: 0x00 无, 0x01 设备编号不匹配, 0x02 枪未处于充电状态, 0x03 其他
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
