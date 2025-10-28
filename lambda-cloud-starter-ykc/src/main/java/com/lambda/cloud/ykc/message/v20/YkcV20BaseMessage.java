package com.lambda.cloud.ykc.message.v20;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.ykc.message.ProtocolMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "base", name = "云快充1.7协议", isPayload = true, description = "云快充1.7协议基础字段")
public abstract class YkcV20BaseMessage<T> implements ProtocolMessage {
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag = "68";

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
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            computed = true,
            littleEndian = true,
            description = "发送时间")
    private LocalDateTime postTime;

    @ProtocolField(
            order = 4,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "加密标志")
    private String encryptFlag;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(order = 6, composite = true, computed = true, description = "内容")
    private T detail;

    @ProtocolField(
            order = 7,
            length = 2,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            crcFiled = true,
            description = "校验码")
    private String crc;
}
