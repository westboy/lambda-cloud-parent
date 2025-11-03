package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 错误报文响应详细信息
 * <p>
 * 帧类型码：0x1B
 * 对应协议文档：GBT-27930充电桩与BMS充电错误报文
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "1B", name = "错误报文", description = "GBT-27930充电桩与BMS充电错误报文")
public class YkcV20BmsErrorRequestDetail {

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
     * 接收SPN2560=0x00的充电机辨识报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "接收SPN2560=0x00的充电机辨识报文超时")
    private Integer chargerIdentificationTimeout00;

    /**
     * 接收SPN2560=0xAA的充电机辨识报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "接收SPN2560=0xAA的充电机辨识报文超时")
    private Integer chargerIdentificationTimeoutAA;

    /**
     * 预留位 (4位)
     * 预留，置0
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved1;

    /**
     * 接收充电机的时间同步和充电机最大输出能力报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "接收充电机的时间同步和充电机最大输出能力报文超时")
    private Integer timeSyncAndMaxOutputTimeout;

    /**
     * 接收充电机完成充电准备报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.UINT8, description = "接收充电机完成充电准备报文超时")
    private Integer chargerReadyTimeout;

    /**
     * 预留位 (4位)
     * 预留，置0
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved2;

    /**
     * 接收充电机充电状态报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 10, length = 1, dataType = ProtocolDataType.UINT8, description = "接收充电机充电状态报文超时")
    private Integer chargerStatusTimeout;

    /**
     * 接收充电机中止充电报文超时 (2位)
     * <00>：=正常；<01>：=超时；<10>：=不可信状态
     */
    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "接收充电机中止充电报文超时")
    private Integer chargerStopTimeout;

    /**
     * 预留位 (4位)
     * 预留，置0
     */
    @ProtocolField(order = 12, length = 1, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved3;
}
