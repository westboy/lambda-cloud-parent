package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电机启动完成详细信息
 *
 * <p>帧类型码：0x4F</p>
 * <p>对应协议文档：8.25 充电机启动完成</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4F", name = "充电机启动完成", description = "充电设备完成启动后上送启动结果与部分电池参数")
public class YkcV20ChargerStartFinishedRequest {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "启动结果")
    private Integer startResult;

    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "失败原因")
    private Integer failReason;

    @ProtocolField(order = 6, length = 5, dataType = ProtocolDataType.HEX, description = "当前电表总值")
    private String currentMeterValue;

    @ProtocolField(order = 7, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "最大允许充电总电压")
    private Integer maxAllowChargeVoltage;

    @ProtocolField(order = 8, length = 3, dataType = ProtocolDataType.HEX, description = "BMS通信协议版本号")
    private String bmsProtocolVersion;

    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS电池类型")
    private Integer batteryType;
}

