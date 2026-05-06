package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 9.5 计费模型设置
 * 对应协议帧类型 0x58（平台->充电桩）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x58", name = "计费模型设置", description = "运营平台下发计费模型（费率与时段费率号）", version = "1.6")
public class YkcV16BillingModelSetDown {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 计费模型编码 (2字节) BCD码 */
    @ProtocolField(order = 2, length = 2, computed = true, dataType = ProtocolDataType.HEX, description = "计费模型编码")
    private String billingModelCode;

    /** 尖电费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 3,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖电费费率")
    private BigDecimal peakElectricityRate;

    /** 尖服务费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 4,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖服务费费率")
    private BigDecimal peakServiceRate;

    /** 峰电费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 5,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "峰电费费率")
    private BigDecimal highElectricityRate;

    /** 峰服务费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "峰服务费费率")
    private BigDecimal highServiceRate;

    /** 平电费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 7,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平电费费率")
    private BigDecimal normalElectricityRate;

    /** 平服务费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 8,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平服务费费率")
    private BigDecimal normalServiceRate;

    /** 谷电费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 9,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷电费费率")
    private BigDecimal valleyElectricityRate;

    /** 谷服务费费率 (4字节) BIN，精确到五位小数 */
    @ProtocolField(
            order = 10,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷服务费费率")
    private BigDecimal valleyServiceRate;

    /** 计损比例 (1字节) BIN */
    @ProtocolField(order = 11, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "计损比例")
    private Integer lossRatio;

    /** 时段费率号数组 (48字节, 每字节1项) BIN: 0x00尖 0x01峰 0x02平 0x03谷 */
    @ProtocolField(
            order = 12,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.UINT8,
            listElementSize = 48,
            description = "时段费率号数组")
    private List<Integer> timeSlotRates;
}
