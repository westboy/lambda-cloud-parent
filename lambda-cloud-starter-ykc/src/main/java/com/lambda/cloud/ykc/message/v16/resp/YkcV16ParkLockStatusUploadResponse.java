package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 10.1 地锁数据上送（上行）
 * 对应协议帧类型 0x61
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x61", name = "地锁数据上送", description = "地锁状态/报警变位上送与周期上送", version = "1.6")
public class YkcV16ParkLockStatusUploadResponse {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BIN码 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    /** 车位锁状态 (1字节) BIN码：0x00 未到位；0x55 升锁到位；0xFF 降锁到位 */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "车位锁状态")
    private Integer parkLockStatus;

    /** 车位状态 (1字节) BIN码：0x00 无车辆；0xFF 停放车辆 */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "车位状态")
    private Integer parkingStatus;

    /** 地锁电量状态 (1字节) BIN码 百分比 0~100 */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "地锁电量状态(%)")
    private Integer parkLockPowerPercent;

    /** 报警状态 (1字节) BIN码：0x00 正常；0xFF 待机摇臂破坏；0x55 摇臂升降异常 */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "报警状态")
    private Integer alarmStatus;

    /** 预留位 (4字节) BIN码，全0 */
    @ProtocolField(order = 7, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}
