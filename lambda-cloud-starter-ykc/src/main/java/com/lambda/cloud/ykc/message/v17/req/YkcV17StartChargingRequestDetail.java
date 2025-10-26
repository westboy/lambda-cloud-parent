package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议充电桩主动申请启动充电消息体
 * <p>
 * 对应协议帧类型 0x31，用户通过帐号密码及刷卡在充电桩上操作请求充电
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolFrame
public class YkcV17StartChargingRequestDetail {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 1,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(
            order = 2,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "枪号")
    private Integer connectorId;

    /**
     * 启动方式 (1字节)
     * 0x01表示通过刷卡启动充电
     * 0x02表求通过帐号启动充电（暂不支持）
     * 0x03表示vin码启动充电
     */
    @ProtocolField(
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "启动方式")
    private Integer startMethod;

    /**
     * 是否需要密码 (1字节)
     * 0x00不需要 0x01需要
     */
    @ProtocolField(
            order = 4,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "是否需要密码")
    private Integer passwordRequired;

    /**
     * 账号或者物理卡号 (8字节)
     * 不足8位补0
     */
    @ProtocolField(
            order = 5,
            length = 8,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "账号或者物理卡号")
    private String accountOrCardNumber;

    /**
     * 输入密码 (16字节)
     * 对用户输入的密码进行16位MD5加密，采用小写上传
     */
    @ProtocolField(
            order = 6,
            length = 16,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "输入密码")
    private String password;

    /**
     * VIN码 (17字节)
     * 启动方式为vin码启动充电时上送,其他方式置零(ASCII码)，VIN码需要反序上送
     */
    @ProtocolField(
            order = 7,
            length = 17,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            littleEndian = true,
            description = "VIN码")
    private String vinCode;

}