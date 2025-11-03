package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议运营平台确认启动充电消息体
 * <p>
 * 对应协议帧类型 0x32，启动充电鉴权结果
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x32", name = "运营平台确认启动充电", description = "启动充电鉴权结果", version = "1.7")
public class YkcV17StartChargingResponseDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 逻辑卡号 (8字节)
     * BCD码，显示在屏幕上，不足8位补0
     */
    @ProtocolField(order = 4, length = 8, computed = true, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 账户余额 (4字节)
     * BIN码，保留两位小数
     */
    @ProtocolField(
            order = 5,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            description = "账户余额")
    private Long accountBalance;

    /**
     * 鉴权成功标志 (1字节)
     * BIN码: 0x00 失败, 0x01 成功
     */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "鉴权成功标志")
    private Integer authenticationResult;

    /**
     * 失败原因 (1字节)
     * BIN码: 0x01 账户不存在, 0x02 账户冻结, 0x03 账户余额不足,
     * 0x04 该卡存在未结账记录, 0x05 桩停用, 0x06 该账户不能在此桩上充电,
     * 0x07 密码错误, 0x08 电站电容不足, 0x09 系统中vin码不存在,
     * 0x0A 该桩存在未结账记录, 0x0B 该桩不支持刷卡
     */
    @ProtocolField(order = 7, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
