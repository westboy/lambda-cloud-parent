package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台远程控制启机请求详细信息
 * <p>
 * 帧类型码：0xA8
 * 对应协议文档：8.3 运营平台远程控制启机
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A8", name = "运营平台远程控制启机", description = "运营平台远程控制启机请求详细信息")
public class YkcV20RemoteStartRequest {

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
     * 显示在屏幕上，不足补零，逻辑卡号为卡面印刷卡号
     */
    @ProtocolField(order = 4, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 物理卡号 (8字节)
     * 不足补零，桩与平台交互需使用的物理卡号
     */
    @ProtocolField(order = 5, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 账户余额 (4字节)
     * 保留到小数点两位
     */
    @ProtocolField(order = 6, length = 4, dataType = ProtocolDataType.UINT32, littleEndian = true, description = "账户余额")
    private Long accountBalance;

    /**
     * 本次充电当前允许的最大功率 (2字节)
     * 单位：kW
     * 默认值0000；当值为0000时按默认最大功率报文下发的功率限制，如无默认最大功率限制则按无限制执行
     */
    @ProtocolField(order = 7, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "本次充电当前允许的最大功率")
    private Integer maxPowerLimit;

    /**
     * SOC限制 (1字节)
     * 默认0x00，不限制
     */
    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.UINT8, description = "SOC限制")
    private Integer socLimit;

    /**
     * 充电电量限制 (4字节)
     * 精确到小数点后四位；默认全0，不限制
     */
    @ProtocolField(order = 9, length = 4, dataType = ProtocolDataType.UINT32, littleEndian = true, description = "充电电量限制")
    private Long energyLimit;
}
