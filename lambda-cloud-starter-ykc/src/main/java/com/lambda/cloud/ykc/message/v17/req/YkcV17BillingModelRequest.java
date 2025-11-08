package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议计费模型请求消息体
 * <p>
 * 对应协议帧类型 0x09，充电桩计费模型请求的消息体部分
 * 样例报文: 68 0E 0000 00 09 32010200000001 01 0100 0069
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x09", name = "计费模型请求", description = "充电桩计费模型请求消息体", version = "1.7")
public class YkcV17BillingModelRequest {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 1,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编号")
    private String equipmentId;

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
     * 计费模型编码 (2字节)
     */
    @ProtocolField(
            order = 3,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "计费模型编码")
    private String billingModelCode;
}
