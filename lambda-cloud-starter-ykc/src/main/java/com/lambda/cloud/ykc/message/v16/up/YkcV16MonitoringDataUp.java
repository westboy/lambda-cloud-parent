package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议上传实时监测数据响应消息体
 * <p>
 * 对应协议帧类型 0x13，上传实时监测数据（充电桩上送）
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x13", name = "上传实时监测数据", description = "上传实时监测数据响应消息体", version = "1.6")
public class YkcV16MonitoringDataUp {

    /**
     * 交易流水号 (16字节)
     * BCD码，见名词解释
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.HEX, description = "交易流水号")
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
     * 状态 (1字节)
     * BIN码: 0x00 离线, 0x01 故障, 0x02 空闲, 0x03 充电
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "状态")
    private Integer status;

    /**
     * 枪是否归位 (1字节)
     * BIN码: 0x00 否, 0x01 是, 0x02 未知
     */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪是否归位")
    private Integer gunInPlace;

    /**
     * 是否插枪 (1字节)
     * BIN码: 0x00 否, 0x01 是
     */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "是否插枪")
    private Integer gunPlugged;

    /**
     * 输出电压 (2字节)
     * 精确到小数点后一位；待机置零
     */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "输出电压")
    private BigDecimal outputVoltage;

    /**
     * 输出电流 (2字节)
     * 精确到小数点后一位；待机置零
     */
    @ProtocolField(
            order = 8,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "输出电流")
    private BigDecimal outputCurrent;

    /**
     * 枪线温度 (1字节)
     * 整形，偏移量-50；待机置零
     */
    @ProtocolField(order = 9, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "枪线温度")
    private Integer cableTemperature;

    /**
     * 枪线编码 (8字节)
     * BIN码，没有置零
     */
    @ProtocolField(order = 10, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "枪线编码")
    private String cableCode;

    /**
     * SOC (1字节)
     * BIN码，待机置零；交流桩置零
     */
    @ProtocolField(order = 11, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "SOC")
    private Integer soc;

    /**
     * 电池组最高温度 (1字节)
     * BIN码，整形，偏移量-50ºC；待机置零；交流桩置零
     */
    @ProtocolField(order = 12, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池组最高温度")
    private Integer batteryMaxTemperature;

    /**
     * 累计充电时间 (2字节)
     * 单位：min；待机置零
     */
    @ProtocolField(
            order = 13,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "累计充电时间")
    private Integer totalChargingTime;

    /**
     * 剩余时间 (2字节)
     * 单位：min；待机置零、交流桩置零
     */
    @ProtocolField(
            order = 14,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "剩余时间")
    private Integer remainingTime;

    /**
     * 充电度数 (4字节)
     * 精确到小数点后四位；待机置零
     */
    @ProtocolField(
            order = 15,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "充电度数")
    private BigDecimal chargingEnergy;

    /**
     * 计损充电度数 (4字节)
     * 精确到小数点后四位；待机置零；未设置计损比例时等于充电度数
     */
    @ProtocolField(
            order = 16,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损充电度数")
    private BigDecimal lossAdjustedEnergy;

    /**
     * 已充金额 (4字节)
     * 精确到小数点后四位；待机置零；（电费+服务费）*计损充电度数
     */
    @ProtocolField(
            order = 17,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "已充金额")
    private BigDecimal chargedAmount;

    /**
     * 硬件故障 (2字节)
     * Bit位表示（0否1是），低位到高位顺序
     */
    @ProtocolField(
            order = 18,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "硬件故障")
    private Integer hardwareFault;
}
