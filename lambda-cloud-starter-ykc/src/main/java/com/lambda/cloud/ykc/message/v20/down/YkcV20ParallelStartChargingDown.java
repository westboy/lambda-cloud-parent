package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台确认并充启动充电
 * <p>
 * 帧类型码：0xA2
 * 对应协议文档：12.2 运营平台确认并充启动充电
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：启动充电鉴权结果，桩需要接收到所有平台并充枪的确认结果
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A2", name = "运营平台确认并充启动充电", description = "运营平台对并充启动充电申请的确认回复")
public class YkcV20ParallelStartChargingDown {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 逻辑卡号 (8字节)
     * BCD码，显示在屏幕上，不足8位补零
     */
    @ProtocolField(order = 4, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 账户余额 (4字节)
     * BIN码，保留两位小数
     */
    @ProtocolField(
            order = 5,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 2,
            description = "账户余额")
    private Integer accountBalance;

    /**
     * 鉴权成功标志 (1字节)
     * 0x00失败
     * 0x01成功
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "鉴权成功标志")
    private Integer authSuccessFlag;

    /**
     * 失败原因 (1字节)
     * 0x01账户不存在
     * 0x02账户冻结
     * 0x03账户余额不足
     * 0x04该卡存在未结账记录
     * 0x05桩停用
     * 0x06该账户不能在此桩上充电
     * 0x07密码错误
     * 0x08电站电容不足
     * 0x09系统中vin码不存在
     * 0x0A该桩存在未结账记录
     * 0x0B该桩不支持刷卡
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;

    /**
     * 并充序号 (6字节)
     * BCD码，0xA1上送并充序号
     */
    @ProtocolField(order = 8, length = 6, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSerialNumber;
}
