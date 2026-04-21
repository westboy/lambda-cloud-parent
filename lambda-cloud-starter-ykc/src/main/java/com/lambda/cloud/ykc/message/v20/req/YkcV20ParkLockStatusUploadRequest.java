package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "61", name = "地锁数据上送", description = "地锁状态/报警信息变化时上送")
public class YkcV20ParkLockStatusUploadRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "枪号")
    private Integer connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "车位锁状态")
    private Integer parkLockStatus;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "车位状态")
    private Integer parkingStatus;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "地锁电量状态")
    private Integer batteryLevel;

    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "报警状态")
    private Integer alarmStatus;

    @ProtocolField(order = 7, length = 4, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;
}

