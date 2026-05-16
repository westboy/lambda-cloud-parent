package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 遥控地锁升锁与降锁命令
 * <p>
 * 帧类型码：0x62
 * 对应协议文档：10.2 遥控地锁升锁与降锁命令
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：服务器下发命令给地锁，地锁执行动作
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "62", name = "遥控地锁升锁与降锁命令", description = "平台向桩下发遥控地锁升锁与降锁命令")
public class YkcV20ParkLockControlDown {

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
     * 升/降地锁 (1字节)
     * 升锁0X55，降锁0XFF
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "升降地锁")
    private String lockAction;

    /**
     * 预留位 (4字节)
     * HEX码，全部置0（可用于多枪）
     */
    @ProtocolField(order = 4, length = 4, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}