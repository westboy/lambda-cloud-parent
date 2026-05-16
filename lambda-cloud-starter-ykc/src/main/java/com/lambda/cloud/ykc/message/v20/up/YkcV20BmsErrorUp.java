package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 错误报文
 * <p>
 * 帧类型码：0x1B
 * 对应协议文档：7.6 错误报文
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：GBT-27930充电桩与BMS充电错误报文
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "1B", name = "错误报文", description = "GBT-27930充电桩与BMS充电错误报文")
public class YkcV20BmsErrorUp {

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
     * 接收SPN2560=0x00的充电机辨识报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS错误报文SPN")
    private Integer bmsErrorSpn;

    /**
     * 接收SPN2560=0xAA的充电机辨识报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "充电机错误报文SPN")
    private Integer chargerErrorSpn;

    /**
     * 预留位 (4位)
     * 0000
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved;
}