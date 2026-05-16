package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩返回数据（上行）
 * <p>
 * 帧类型码：0x63
 * 对应协议文档：10.3 充电桩返回数据（上行）
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：地锁收到遥控地锁升锁与降锁命令指令，响应本数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "63", name = "充电桩返回数据", description = "地锁收到遥控命令后响应")
public class YkcV20ParkLockReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，充电桩资产编号
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 地锁控制返回标志 (1字节)
     * 布尔型（1，鉴权成功；0，鉴权失败）
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "地锁控制返回标志")
    private Integer controlResult;

    /**
     * 预留位 (4字节)
     * HEX码，全部置0（可用于多枪）
     */
    @ProtocolField(order = 4, length = 4, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
