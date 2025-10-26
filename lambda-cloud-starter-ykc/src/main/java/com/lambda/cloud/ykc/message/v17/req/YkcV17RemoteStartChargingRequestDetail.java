package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议运营平台远程控制启机消息体
 * <p>
 * 对应协议帧类型 0x34，当用户通过远程启动充电时，发送本命令
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolFrame
public class YkcV17RemoteStartChargingRequestDetail {

    /**
     * 交易流水号 (16字节)
     */
    @ProtocolField(
            order = 1,
            length = 16,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 2,
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
            order = 3,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "枪号")
    private Integer connectorId;

    /**
     * 逻辑卡号 (8字节)
     * 显示在屏幕上，不足补零,逻辑卡号为卡面印刷卡号
     */
    @ProtocolField(
            order = 4,
            length = 8,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 物理卡号 (8字节)
     * 不足补零，桩与平台交互需使用的物理卡号
     */
    @ProtocolField(
            order = 5,
            length = 8,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 账户余额 (4字节)
     * 保留到小数点两位
     */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.DOUBLE,
            littleEndian = true,
            description = "账户余额")
    private Float accountBalance;

}