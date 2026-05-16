package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程并充启机命令回复
 * <p>
 * 帧类型码：0xA3
 * 对应协议文档：12.4 远程并充启机命令回复
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：远程启动充电命令回复，并充多枪指令回复应一致
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A3", name = "远程并充启机命令回复", description = "桩向平台回复远程并充启机命令结果")
public class YkcV20ParallelRemoteStartReplyUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 启动结果 (1字节)
     * 0x00失败
     * 0x01成功
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "启动结果")
    private Integer startResult;

    /**
     * 失败原因 (1字节)
     * 0x00无
     * 0x01设备编号不匹配
     * 0x02枪已在充电
     * 0x03设备故障
     * 0x04设备离线
     * 0x05未插枪
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;

    /**
     * 主辅枪标记 (1字节)
     * 0x00主枪
     * 0x01辅枪
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "主辅枪标记")
    private Integer masterSlaveMark;

    /**
     * 并充序号 (6字节)
     * BCD码，0xA4下发的并充序号
     */
    @ProtocolField(order = 7, length = 6, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSerialNumber;
}