package com.lambda.cloud.ykc.protocol.elec;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x23", name = "需求电流", description = "消息体", version = "1.0")
public class YkcV16ElecDetail {

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 1,
            length = 16,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String orderNumber;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 2,
            length = 7,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String equNumber;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String gunNumber;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 4,
            length = 2,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String d1;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private Integer d2;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 5,
            length = 12,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String s2;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.BIT,
            description = "订单编号")
    private String s21;

    /**
     * 订单编号 (16字节)
     */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            littleEndian = true,
            dataType = ProtocolDataType.HEX,
            description = "订单编号")
    private String s22;
}
