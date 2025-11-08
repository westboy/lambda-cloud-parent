package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议登录请求消息体
 * <p>
 * 对应协议帧类型 0x01，充电桩登录认证请求
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x01", name = "登录请求", description = "充电桩登录认证请求消息体", version = "1.6")
public class YkcV16LoginRequest {

    /**
     * 桩编码 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

    /**
     * 桩类型 (1字节)
     * BIN码: 0表示直流桩，1表示交流桩
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "桩类型")
    private Integer equipmentType;

    /**
     * 充电枪数量 (1字节)
     * BIN码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "充电枪数量")
    private Integer connectorCount;

    /**
     * 通信协议版本 (1字节)
     * BIN码，版本号乘10，v1.0表示0x0A
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "通信协议版本")
    private Integer protocolVersion;

    /**
     * 程序版本 (8字节)
     * ASCII码，不足8位补零
     */
    @ProtocolField(order = 5, length = 8, computed = true, dataType = ProtocolDataType.ASCII, description = "程序版本")
    private String programVersion;

    /**
     * 网络链接类型 (1字节)
     * BIN码: 0x00 SIM卡, 0x01 LAN, 0x02 WAN, 0x03 其他
     */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "网络链接类型")
    private Integer networkType;

    /**
     * Sim卡 (10字节)
     * BCD码，不足10位补零，取不到置零
     */
    @ProtocolField(order = 7, length = 10, computed = true, dataType = ProtocolDataType.BCD, description = "Sim卡")
    private String simCardNumber;

    /**
     * 运营商 (1字节)
     * BIN码: 0x00 移动, 0x02 电信, 0x03 联通, 0x04 其他
     */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "运营商")
    private Integer operator;
}
