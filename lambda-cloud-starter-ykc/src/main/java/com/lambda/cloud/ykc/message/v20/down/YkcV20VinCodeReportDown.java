package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩上报vin码回复
 * <p>
 * 帧类型码：0xAA
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：运营平台对桩上报vin码的回复
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "AA", name = "充电桩上报vin码回复", description = "运营平台对桩上报vin码的回复")
public class YkcV20VinCodeReportDown {

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
     * 确认结果 (1字节)
     * BIN码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
