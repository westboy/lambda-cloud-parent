package com.lambda.cloud.ykc.message.v20;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.message.ProtocolMessage;
import com.lambda.cloud.netty.protocol.message.RawPayloadAware;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议基础消息类
 *
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "云快充2.0基础协议", isFrame = true, description = "云快充平台协议V2.1.0基础字段")
public class YkcV20BasePayload implements ProtocolMessage, RawPayloadAware {

    /**
     * 起始符(1字节)
     * 固定值：0x68
     */
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    /**
     * 数据长度 (1字节)
     * 表示从序列号域到校验码前的数据长度
     */
    @ProtocolField(
            order = 1,
            length = 1,
            dataType = ProtocolDataType.HEX,
            lengthFiled = true,
            littleEndian = true,
            description = "数据长度")
    private Integer dataLength;

    /**
     * 序列号域 (2字节)
     * 用于标识消息的序列号，小端序
     */
    @ProtocolField(
            order = 2,
            length = 2,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "序列号域")
    private Integer serialNumber;

    /**
     * 加密标志 (1字节)
     * 0x00：未加密，0x01：加密
     */
    @ProtocolField(
            order = 3,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            encryptedKey = true,
            description = "加密标志")
    private String encryptFlag;

    /**
     * 帧类型(1字节)
     * 表示消息的类型，不同的帧类型对应不同的消息内容
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    /**
     * 数据域(可变长度)
     * 具体的消息内容，根据帧类型的不同而不同
     */
    @ProtocolField(
            order = 5,
            composite = true,
            payload = true,
            computed = true,
            description = "数据域",
            encryptedField = true)
    private Object detail;

    /**
     * 校验码(2字节)
     * 从起始符到数据域的所有字节的异或校验码
     */
    @ProtocolField(
            order = 6,
            length = 2,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            crcFiled = true,
            description = "校验码")
    private String crc;

    private byte[] rawPayload;

    @Override
    public byte[] getRawPayload() {
        return rawPayload;
    }

    @Override
    public void setRawPayload(byte[] rawPayload) {
        this.rawPayload = rawPayload;
    }

    @Override
    public String getFrameType() {
        return frameType == null ? null : frameType.toUpperCase();
    }
}
