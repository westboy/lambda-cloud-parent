package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 设备故障复位上送详细信息
 *
 * <p>帧类型码：0x4B</p>
 * <p>对应协议文档：8.21 设备故障复位上送</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4B", name = "设备故障复位上送", description = "故障恢复后立即上送复位信息")
public class YkcV20FaultResetReportRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "故障类型")
    private Integer faultType;

    @ProtocolField(order = 4, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "故障编码")
    private Integer faultCode;

    @ProtocolField(order = 5, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "故障复位时间")
    private LocalDateTime resetTime;
}
