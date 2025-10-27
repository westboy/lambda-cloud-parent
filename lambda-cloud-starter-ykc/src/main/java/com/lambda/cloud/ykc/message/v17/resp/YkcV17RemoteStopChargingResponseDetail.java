package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议远程停机命令回复消息体
 * <p>
 * 对应协议帧类型 0x35，远程停机命令回复
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "0x35", name = "远程停机命令回复", description = "远程停机命令回复", version = "1.7")
public class YkcV17RemoteStopChargingResponseDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 停止结果 (1字节)
     * BIN码: 0x00 失败, 0x01 成功
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "停止结果")
    private Integer stopResult;

    /**
     * 失败原因 (1字节)
     * BIN码: 0x00 无, 0x01 设备编号不匹配, 0x02 枪未在充电,
     * 0x03 设备故障, 0x04 设备离线
     */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
