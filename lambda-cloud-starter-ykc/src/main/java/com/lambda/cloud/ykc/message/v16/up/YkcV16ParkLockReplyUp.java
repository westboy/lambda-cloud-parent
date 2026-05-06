package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 地锁返回数据
 * 对应协议帧类型 0x63（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x63", name = "地锁返回数据", description = "地锁控制命令执行结果返回", version = "1.6")
public class YkcV16ParkLockReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BIN码
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    /**
     * 控制结果 (1字节)
     * BIN码: 0x00 成功, 0x01 失败
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "控制结果")
    private Integer controlResult;

    /** 预留位 (4字节) BIN码，全部置0 */
    @ProtocolField(order = 4, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
