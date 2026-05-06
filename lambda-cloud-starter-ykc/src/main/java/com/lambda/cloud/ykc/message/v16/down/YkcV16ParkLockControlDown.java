package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 10.2 遥控地锁升锁与降锁命令
 * 对应协议帧类型 0x62（平台->充电桩）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x62", name = "遥控地锁升锁与降锁命令", description = "服务器下发地锁升降命令", version = "1.6")
public class YkcV16ParkLockControlDown {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BIN码 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    /** 升/降地锁 (1字节) BIN：升锁0x55，降锁0xFF */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "升/降地锁")
    private Integer controlAction;

    /** 预留位 (4字节) BIN码，全0 */
    @ProtocolField(order = 4, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
