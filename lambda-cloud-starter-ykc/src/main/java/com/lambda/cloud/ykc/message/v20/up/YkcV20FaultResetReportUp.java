package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 设备故障复位上送
 * <p>
 * 帧类型码：0x4B
 * 对应协议文档：8.21 设备故障复位上送
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：设备在运行过程中，之前发生故障恢复后，立即上送
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4B", name = "设备故障复位上送", description = "故障恢复后立即上送复位信息")
public class YkcV20FaultResetReportUp {

    /**
     * 桩编号 (7字节)
     * BCD码
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
     * 故障类型 (1字节)
     * 0x01车故障
     * 0x02车桩交互故障
     * 0x03桩/平台故障
     * 0x04桩故障
     * 0x05自定义故障
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "故障类型")
    private Integer faultType;

    /**
     * 故障编码 (2字节)
     * 详情见附录13.2
     */
    @ProtocolField(order = 4, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "故障编码")
    private Integer faultCode;

    /**
     * 故障复位时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(order = 5, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "故障复位时间")
    private LocalDateTime resetTime;
}
