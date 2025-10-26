package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议心跳请求消息体
 * <p>
 * 对应协议帧类型 0x03，充电桩心跳包的消息体部分
 * 样例报文: 68（起始标志）0D（数据长度）0001（序列号域）00（加密标志）03（类型）32010200000001（桩编码）0x01（枪号：1号枪）00（枪状态：正常）6890（帧校验域）
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x03", name = "心跳请求", description = "充电桩心跳包消息体", version = "1.7")
public class YkcV17HeartbeatRequestDetail {

    /**
     * 桩编码 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 1,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编码")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(
            order = 2,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "枪号")
    private Integer connectorId;

    /**
     * 枪状态 (1字节)
     * 0x00：正常
     * 0x01：故障
     */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "枪状态")
    private String connectorStatus;

    /**
     * 默认构造函数
     */
    public YkcV17HeartbeatRequestDetail() {}

}
