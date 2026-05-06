package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 远程并充启机命令回复
 * 对应协议帧类型 0xA3（充电桩 -> 运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xA3", name = "远程并充启机命令回复", description = "远程并充启动充电命令回复", version = "1.6")
public class YkcV16ParallelRemoteStartReplyUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 启动结果 (1字节)
     * BIN码: 0x00 失败, 0x01 成功
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "启动结果")
    private Integer startResult;

    /**
     * 失败原因 (1字节)
     * BIN码: 0x00 无, 0x01 设备编号不匹配, 0x02 枪已在充电, 0x03 设备故障, 0x04 设备离线, 0x05 未插枪
     */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;

    /**
     * 主辅枪标记 (1字节)
     * BIN码: 0x00 主枪, 0x01 辅枪
     */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "主辅枪标记")
    private Integer masterSlaveFlag;

    /**
     * 并充序号 (6字节)
     * BCD码: 0xA4下发的并充序号
     */
    @ProtocolField(order = 7, length = 6, computed = true, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}
