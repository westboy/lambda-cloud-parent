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
@ProtocolPayload(frameType = "A1", name = "充电桩主动申请并充充电", description = "充电桩多枪并充模式启动请求")
public class YkcV20ParallelStartChargingRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "启动方式")
    private Integer startMode;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "是否需要密码")
    private Integer needPassword;

    @ProtocolField(order = 5, length = 8, dataType = ProtocolDataType.HEX, description = "账号或者物理卡号")
    private String accountOrCardNumber;

    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.HEX, description = "输入密码")
    private String inputPassword;

    @ProtocolField(order = 7, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vinCode;

    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.UINT8, description = "主辅枪标记")
    private Integer mainOrSubGunFlag;

    @ProtocolField(order = 9, length = 6, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}
