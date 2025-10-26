package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议计费模型响应消息体
 * <p>
 * 对应协议帧类型 0x0A，计费模型请求应答的消息体部分
 * 包含各种费率信息和48个时段费率号（每30分钟一个时段）
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x0A", name = "计费模型响应", description = "计费模型请求应答消息体", version = "1.7")
public class YkcV17BillingModelResponseDetail {

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
     * 计费模型编号 (2字节)
     * 固定值：01 00
     */
    @ProtocolField(
            order = 3,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "计费模型编号")
    private String billingModelCode;

    /**
     * 尖费电费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 4,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖费电费费率")
    private Long peakElectricityRate;

    /**
     * 尖服务费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 5,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖服务费费率")
    private Long peakServiceRate;

    /**
     * 峰电费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "峰电费费率")
    private Long highElectricityRate;

    /**
     * 峰服务费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 7,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "峰服务费费率")
    private Long highServiceRate;

    /**
     * 平电费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 8,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平电费费率")
    private Long normalElectricityRate;

    /**
     * 平服务费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 9,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平服务费费率")
    private Long normalServiceRate;

    /**
     * 谷电费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 10,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷电费费率")
    private Long valleyElectricityRate;

    /**
     * 谷服务费费率 (4字节)
     * 精确到五位小数
     */
    @ProtocolField(
            order = 11,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷服务费费率")
    private Long valleyServiceRate;

    /**
     * 计损比例 (1字节)
     */
    @ProtocolField(
            order = 12,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            littleEndian = true,
            description = "计损比例")
    private Integer lossRatio;

    /**
     * 时段费率号数组 (48字节)
     * 每30分钟一个时段，一天48个时段
     * 0x00：尖费率 0x01：峰费率 0x02：平费率 0x03：谷费率
     */
    @ProtocolField(
            order = 13,
            length = 48,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "时段费率号数组")
    private String timeSlotRates;
}
