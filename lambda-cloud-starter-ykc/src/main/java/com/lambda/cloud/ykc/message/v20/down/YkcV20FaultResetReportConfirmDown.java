package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 设备故障复位上送回复确认
 * <p>
 * 帧类型码：0x4A
 * 对应协议文档：8.22 设备故障复位上送回复确认
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台在接收到故障复位上送后，对上送信息进行回复
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4A", name = "设备故障复位上送回复确认", description = "平台对设备故障复位上送的回复确认")
public class YkcV20FaultResetReportConfirmDown {

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
     * 确认接收标记 (1字节)
     * 0x00上传成功
     * 0x01非法故障
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
