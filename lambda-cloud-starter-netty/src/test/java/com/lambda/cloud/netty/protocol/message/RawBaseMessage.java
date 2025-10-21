package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;

/**
 * 交易记录消息
 * <p>
 * 对应协议帧类型 0x3B，包含充电交易的详细信息
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ProtocolFrame(frameType = "base", name = "云快充基础协议", description = "云快充基础协议字段")
public class RawBaseMessage {

    /**
     * 报文头 - 起始符 (68)
     */
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX, littleEndian = true, description = "数据长度")
    private Integer dataLength;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(
            order = 2,
            length = 2,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "数据长度")
    private Integer ser;

    /**
     * 报文头 - 数据长度
     */
    @ProtocolField(
            order = 3,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "数据长度")
    private String sec;

    /**
     * 报文头 - 帧类型 (0x3B)
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(order = 5, composite = true, computed = true, encryptedField = true)
    private RawInnerRecord innerRecord;
    /**
     * 校验码 (2字节)
     */
    @ProtocolField(
            order = 6,
            length = 2,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            CrcFiled = true,
            description = "校验码")
    private String checksum;
}
