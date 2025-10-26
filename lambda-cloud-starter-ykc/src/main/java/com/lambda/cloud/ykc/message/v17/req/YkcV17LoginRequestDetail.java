package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议登录请求消息体
 * <p>
 * 对应协议帧类型 0x01，充电桩登录认证请求的消息体部分
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x01", name = "登录请求", description = "充电桩登录认证请求消息体", version = "1.7")
public class YkcV17LoginRequestDetail {

    /**
     * 桩编号 (7字节)
     */
    @ProtocolField(
            order = 1,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编号")
    private String equipmentId;

    /**
     * 桩类型 (1字节)
     * 01: 直流桩
     * 02: 交流桩
     */
    @ProtocolField(
            order = 2,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            littleEndian = true,
            description = "桩类型")
    private Integer equipmentType;

    /**
     * 枪数量 (1字节)
     */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "枪数量")
    private Integer connectorCount;

    /**
     * 协议版本 (2字节)
     * 17: 表示1.7版本
     */
    @ProtocolField(
            order = 4,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "协议版本")
    private String protocolVersion;

    /**
     * 程序版本 (4字节)
     */
    @ProtocolField(
            order = 5,
            length = 8,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            littleEndian = true,
            description = "程序版本")
    private String programVersion;

    /**
     * 网络类型 (1字节)
     * 01: 有线网络
     * 02: 无线网络
     */
    @ProtocolField(
            order = 6,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "网络类型")
    private String networkType;

    /**
     * SIM卡号 (11字节)
     */
    @ProtocolField(
            order = 7,
            length = 10,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "SIM卡号")
    private String simCardNumber;

    /**
     * 运营商 (1字节)
     * 01: 中国移动
     * 02: 中国联通
     * 03: 中国电信
     */
    @ProtocolField(
            order = 8,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "运营商")
    private String operator;
}
