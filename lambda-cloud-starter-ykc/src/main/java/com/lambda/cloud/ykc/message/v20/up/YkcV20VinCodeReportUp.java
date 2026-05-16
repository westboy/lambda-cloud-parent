package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩上报vin码
 * <p>
 * 帧类型码：0xA9
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：当充电枪启动成功后，桩进行车桩交互读取车辆vin码，识别到vin码后上送
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A9", name = "充电桩上报vin码", description = "充电桩收到插枪后，完成采集作业后上送vin码")
public class YkcV20VinCodeReportUp {

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
     * VIN码 (17字节)
     * ASCII码，不足17位补0
     */
    @ProtocolField(order = 3, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vin;
}
