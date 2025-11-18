package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 9.1 充电桩工作参数设置
 * 对应协议帧类型 0x52
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x52", name = "充电桩工作参数设置", description = "远程设置充电桩是否停用、最大允许输出功率", version = "1.6")
public class YkcV16WorkingParamsSetRequest {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 是否允许工作 (1字节) BIN码: 0x00 允许；0x01 停用 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "是否允许工作")
    private Integer allowWork;

    /** 最大允许输出功率 (1字节) BIN码，1BIN表示1%，最大100%，最小30% */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "最大允许输出功率(%)")
    private Integer maxOutputPowerPercent;
}
