package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 地锁数据上送
 * <p>
 * 帧类型码：0x61
 * 对应协议文档：10.1 地锁数据上送
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：地锁状态/报警信息变化时，桩立刻上送变位/报警信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "61", name = "地锁数据上送", description = "地锁状态/报警信息变化时上送")
public class YkcV20ParkLockStatusUploadUp {

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
     * 车位锁状态 (1字节)
     * 0x00：未到位状态
     * 0x55：升锁到位状态
     * 0xFF：降锁到位状态
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "车位锁状态")
    private Integer parkLockStatus;

    /**
     * 车位状态 (1字节)
     * 0x00：无车辆
     * 0xFF：停放车辆
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "车位状态")
    private Integer parkingSpaceStatus;

    /**
     * 地锁电量状态 (1字节)
     * 百分比值0~100
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "地锁电量状态")
    private Integer parkLockBatteryLevel;

    /**
     * 报警状态 (1字节)
     * 0x00：正常无报警
     * 0xFF：待机状态摇臂破坏
     * 0x55：摇臂升降异常(未到位)
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "报警状态")
    private Integer alarmStatus;

    /**
     * 预留位 (4字节)
     * HEX码，全部置0
     */
    @ProtocolField(order = 7, length = 4, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
