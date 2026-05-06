package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 错误报文（BMS/充电机状态位上送）
 * 对应协议帧类型 0x1B（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x1B", name = "错误报文", description = "错误报文状态位上送（分组bit位）", version = "1.6")
public class YkcV16ErrorReportUp {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 4-6 组 (1字节) */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "序号4-6状态组")
    private Integer group0406;

    /** 7-9 组 (1字节) */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "序号7-9状态组")
    private Integer group0709;

    /** 10-12 组 (1字节) */
    @ProtocolField(
            order = 6,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号10-12状态组")
    private Integer group1012;

    /** 13-14 组 (1字节) */
    @ProtocolField(
            order = 7,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号13-14状态组")
    private Integer group1314;

    /** 15-16 组 (1字节) */
    @ProtocolField(
            order = 8,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号15-16状态组")
    private Integer group1516;

    /** 17-19 组 (1字节) */
    @ProtocolField(
            order = 9,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号17-19状态组")
    private Integer group1719;

    /** 20-23 组 (1字节) */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号20-23状态组")
    private Integer group2023;

    /** 24-25 组 (1字节) */
    @ProtocolField(
            order = 11,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "序号24-25状态组")
    private Integer group2425;
}
