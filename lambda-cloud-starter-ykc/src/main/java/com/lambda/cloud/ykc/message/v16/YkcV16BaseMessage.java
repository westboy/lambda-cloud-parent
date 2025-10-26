package com.lambda.cloud.ykc.message.v16;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "base", name = "云快充基础协议", isPayload = true, description = "云快充基础协议字段")
public abstract class YkcV16BaseMessage<T> {

    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    @ProtocolField(
            order = 1,
            length = 1,
            dataType = ProtocolDataType.HEX,
            lengthFiled = true,
            littleEndian = true,
            description = "数据长度")
    private Integer dataLength;

    @ProtocolField(
            order = 2,
            length = 2,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "序列号域")
    private Integer serialNumber;

    @ProtocolField(
            order = 3,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "加密标志")
    private String encryptFlag;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(order = 5, composite = true, computed = true, description = "内容")
    private T body;

    @ProtocolField(
            order = 6,
            length = 2,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            crcFiled = true,
            description = "校验码")
    private String crc;
}
