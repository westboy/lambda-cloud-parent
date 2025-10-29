package com.lambda.cloud.ykc.message.v20;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.ykc.message.ProtocolMessage;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议基础消息类
 * <p>
 * 定义云快充平台协议V2.1.0的基础消息结构，包含协议头部的公共字段
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "base", name = "云快充2.0协议", isPayload = true, description = "云快充2.0协议基础字段")
public abstract class YkcV20BaseMessage<T> implements ProtocolMessage {

    /**
     * 起始符(1字节)
     * 固定值：0x68
     */
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag = "68";

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
     * 发送时间(7字节)
     * 消息发送的时间，格式为CP56TIME2A，小端序
     */
    @ProtocolField(
            order = 3,
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            computed = true,
            littleEndian = true,
            description = "发送时间")
    private LocalDateTime postTime;

    /**
     * 加密标志 (1字节)
     * 0x00：未加密，0x01：加密
     */
    @ProtocolField(
            order = 4,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "加密标志")
    private String encryptFlag;

    /**
     * 帧类型(1字节)
     * 表示消息的类型，不同的帧类型对应不同的消息内容
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    /**
     * 数据域(可变长度)
     * 具体的消息内容，根据帧类型的不同而不同
     */
    @ProtocolField(order = 6, composite = true, computed = true, description = "数据域")
    private T detail;

    /**
     * 校验码(1字节)
     * 从起始符到数据域的所有字节的异或校验码
     */
    @ProtocolField(
            order = 7,
            length = 1,
            dataType = ProtocolDataType.HEX,
            computed = true,
            littleEndian = true,
            description = "校验码")
    private String crc;
}
