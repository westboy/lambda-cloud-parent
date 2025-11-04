package com.lambda.cloud.ykc.message.v16;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@com.lambda.cloud.netty.protocol.annotation.ProtocolPayload(
        frameType = "base",
        name = "云快充基础协议",
        isFrame = true,
        description = "云快充基础协议字段")
public class YkcV16BasePayload implements com.lambda.cloud.netty.protocol.message.ProtocolMessage {

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
            encryptedKey = true,
            description = "加密标志")
    private String encryptFlag;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(
            order = 5,
            composite = true,
            payload = true,
            computed = true,
            description = "数据域",
            encryptedField = true)
    private Object detail;

    @ProtocolField(
            order = 6,
            length = 2,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            crcFiled = true,
            description = "校验码")
    private String crc;
}
