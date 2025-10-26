package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议远程启动充电命令回复消息体
 * <p>
 * 对应协议帧类型 0x33，远程启动充电命令回复
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolFrame
public class YkcV17RemoteStartChargingResponseDetail {

    /**
     * 交易流水号 (16字节)
     */
    @ProtocolField(
            order = 1,
            length = 16,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 2,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "枪号")
    private Integer connectorId;

    /**
     * 启动结果 (1字节)
     * 0x00失败 0x01成功
     */
    @ProtocolField(
            order = 4,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "启动结果")
    private Integer startResult;

    /**
     * 失败原因 (1字节)
     * 0x00无
     * 0x01设备编号不匹配
     * 0x02枪已在充电
     * 0x03设备故障
     * 0x04设备离线
     * 0x05未插枪
     */
    @ProtocolField(
            order = 5,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "失败原因")
    private Integer failureReason;
}