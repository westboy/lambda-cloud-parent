package com.lambda.cloud.netty.protocol.checksum;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;

/**
 * CRC测试消息类
 * 用于测试CRC校验功能
 *
 * @author lambda
 * @since 2024-01-01
 */
@Getter
@ProtocolFrame
public class CrcTestMessage {

    // Getters and Setters
    /**
     * 消息头
     */
    @ProtocolField(order = 1, dataType = ProtocolDataType.HEX, length = 2)
    private String header = "AA55";

    /**
     * 消息类型
     */
    @ProtocolField(order = 2, dataType = ProtocolDataType.UINT8)
    private int messageType = 0x01;

    /**
     * 数据长度
     */
    @ProtocolField(order = 3, dataType = ProtocolDataType.UINT16)
    private int dataLength = 10;

    /**
     * 数据内容
     */
    @ProtocolField(order = 4, dataType = ProtocolDataType.HEX, length = 10)
    private String data = "1234567890ABCDEF1234";

    /**
     * CRC16校验和
     */
    //    @ProtocolField(order = 5, dataType = ProtocolDataType.CRC, length = 2, CRCChecksum = true)
    private int crc;

    public void setHeader(String header) {
        this.header = header;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    @Override
    public String toString() {
        return "CrcTestMessage{" + "header='"
                + header + '\'' + ", messageType="
                + messageType + ", dataLength="
                + dataLength + ", data='"
                + data + '\'' + ", crc="
                + String.format("0x%04X", crc) + '}';
    }
}
