package com.lambda.cloud.t645.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.message.RawPayloadAware;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 多功能电能表通信协议帧结构。
 *
 * <p>帧格式（字节序）：</p>
 * <pre>
 * | 起始符(1B) | 地址域(6B,BCD) | 起始符(1B) | 控制码(1B) | 数据长度(1B) | 数据域(NB) | 校验和CS(1B) | 结束符(1B) |
 * |    0x68     |   A0~A5       |    0x68     |    C       |     L        |  DATA     |   SUM8      |    0x16    |
 * </pre>
 *
 * <p>地址域采用低字节在前（Little-Endian）传输；数据域发送时每个字节加 0x33 偏移。</p>
 *
 * @see T645DataBody
 * @see T645PayloadRegistry
 */
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

    /** 帧起始符1，固定为 0x68。 */
    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "起始符1")
    private String startFlag1;

    /** 地址域（6 字节 BCD 编码），传输序为低字节在前。 */
    @ProtocolField(
            order = 1,
            length = 6,
            dataType = ProtocolDataType.BCD,
            computed = true,
            description = "地址域（传输序：低字节在前）")
    private String address;

    /** 帧起始符2，固定为 0x68。 */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "起始符2")
    private String startFlag2;

    /** 控制码，标识报文类型（读/写/应答/广播等）。 */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, computed = true, description = "控制码")
    private Integer controlCode;

    /** 数据域长度（字节数）。 */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, computed = true, description = "数据域长度")
    private Integer dataLength;

    /** 数据域原始字节（未去除 +0x33 偏移）。 */
    @ProtocolField(order = 5, dataType = ProtocolDataType.HEX, computed = true, description = "数据域（原始字节，未偏移）")
    private byte[] data;

    /** 校验和 CS，从起始符到数据域所有字节累加取低 8 位。 */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, crcFiled = true, description = "校验和CS")
    private Integer cs;

    /** 帧结束符，固定为 0x16。 */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.HEX, description = "结束符")
    private String endFlag;

    /** 原始报文载荷字节（由 Netty 解码层设置）。 */
    private transient byte[] rawPayload;

    /**
     * 业务分发层解码后存储的实体对象（如 T645HeartbeatRequest 等）。
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

    /**
     * 同步数据域长度字段，将 {@link #data} 的实际长度赋值给 {@link #dataLength}。
     *
     * <p>编码发送帧时需调用此方法确保数据长度字段正确。</p>
     */
    public void syncDataLength() {
        if (data != null) {
            this.dataLength = data.length;
        }
    }
}
