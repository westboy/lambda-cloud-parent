package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台确认启动充电响应详细信息
 * <p>
 * 帧类型码：0xA6
 * 对应协议文档：8.2 运营平台确认启动充电
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A6", name = "运营平台确认启动充电", description = "运营平台确认启动充电响应详细信息")
public class YkcV20StartChargingConfirmResponse {

    /**
     * 交易流水号 (16字节)
     * 见名词解释
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 逻辑卡号 (8字节)
     * 显示在屏幕上，不足8位补零
     */
    @ProtocolField(order = 4, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 账户余额 (4字节)
     * 保留两位小数
     */
    @ProtocolField(order = 5, length = 4, dataType = ProtocolDataType.UINT32, description = "账户余额")
    private Integer accountBalance;

    /**
     * 本次充电当前允许的最大功率 (2字节)
     * 单位：kW
     * 默认值0000；当值为0000时按默认最大功率报文下发的功率限制，如无默认最大功率限制则按无限制执行
     */
    @ProtocolField(order = 6, length = 2, dataType = ProtocolDataType.UINT16, description = "本次充电当前允许的最大功率")
    private Integer maxPowerLimit;

    /**
     * SOC限制 (1字节)
     * 默认0x00，不限制
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "SOC限制")
    private Integer socLimit;

    /**
     * 充电电量限制 (4字节)
     * 精确到小数点后四位；默认全0，不限制
     */
    @ProtocolField(order = 8, length = 4, dataType = ProtocolDataType.UINT32, description = "充电电量限制")
    private Integer energyLimit;

    /**
     * 鉴权成功标志 (1字节)
     * 0x00失败 0x01成功
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "鉴权成功标志")
    private Integer authenticationSuccess;

    /**
     * 失败原因 (1字节)
     * 0x01账户不存在，0x02账户冻结，0x03账户余额不足，0x04该卡存在未结账记录
     * 0x05桩停用，0x06该账户不能在此桩上充电，0x07密码错误，0x08电站电容不足
     * 0x09系统中vin码不存在，0x0A该桩存在未结账记录，0x0B该桩不支持刷卡
     */
    @ProtocolField(order = 10, length = 1, dataType = ProtocolDataType.BCD, description = "失败原因")
    private String failureReason;
}
