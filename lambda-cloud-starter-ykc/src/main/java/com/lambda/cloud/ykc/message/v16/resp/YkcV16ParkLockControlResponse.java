package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 10.3 充电桩返回数据（上行）
 * 对应协议帧类型 0x63
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x63", name = "地锁控制返回（上行）", description = "地锁控制返回标志与预留位", version = "1.6")
public class YkcV16ParkLockControlResponse {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BIN码 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    /** 地锁控制返回标志 (1字节) BIN码：布尔型（1成功；0失败） */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "控制返回标志")
    private Integer controlResult;

    /** 预留位 (4字节) BIN码，全0 */
    @ProtocolField(order = 4, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
