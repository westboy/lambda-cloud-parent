package com.lambda.cloud.t645.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.message.RawPayloadAware;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(
        frameType = "T645-07",
        name = "DL/T645-2007帧",
        crcAlgorithm = "SUM8",
        isFrame = true,
        description = "DL/T 645-2007 多功能电能表通信协议帧")
public class T645Frame implements RawPayloadAware {

    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "起始符1")
    private String startFlag1;

    @ProtocolField(
            order = 1,
            length = 6,
            dataType = ProtocolDataType.BCD,
            computed = true,
            description = "地址域（传输序：低字节在前）")
    private String address;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "起始符2")
    private String startFlag2;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, computed = true, description = "控制码")
    private Integer controlCode;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, computed = true, description = "数据域长度")
    private Integer dataLength;

    @ProtocolField(order = 5, dataType = ProtocolDataType.HEX, computed = true, description = "数据域（原始字节，未偏移）")
    private byte[] data;

    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, crcFiled = true, description = "校验和CS")
    private Integer cs;

    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.HEX, description = "结束符")
    private String endFlag;

    private transient byte[] rawPayload;

    /**
     * 业务分发层解码后存储的实体对象（如 T645HeartbeatRequest 等）
     */
    private transient Object body;

    @Override
    public byte[] getRawPayload() {
        return rawPayload;
    }

    @Override
    public void setRawPayload(byte[] rawPayload) {
        this.rawPayload = rawPayload;
    }

    public void syncDataLength() {
        if (data != null) {
            this.dataLength = data.length;
        }
    }
}
