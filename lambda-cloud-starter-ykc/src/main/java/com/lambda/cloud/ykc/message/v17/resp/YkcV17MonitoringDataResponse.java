package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议上传实时监测数据响应消息体
 * <p>
 * 对应协议帧类型 0x13，上传实时监测数据的消息体部分
 * 包含充电枪的各种实时监测数据
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x13", name = "上传实时监测数据", description = "上传实时监测数据响应消息体", version = "1.7")
public class YkcV17MonitoringDataResponse {

    /**
     * 交易流水号 (16字节)
     * 见名词解释
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
     * 状态 (1字节)
     * BIN码: 0x00 离线, 0x01 故障, 0x02 空闲, 0x03 充电
     * 需做到变位上送
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
     * 需做到变位上送
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
    private Integer outputVoltage;

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
    private Integer outputCurrent;

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
    private Long chargingEnergy;

    /**
     * 计损充电度数 (4字节)
     * 精确到小数点后四位；待机置零
     * 未设置计损比例时等于充电度数
     */
    @ProtocolField(
            order = 16,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损充电度数")
    private Long lossAdjustedEnergy;

    /**
     * 已充金额 (4字节)
     * 精确到小数点后四位；待机置零
     * （电费+服务费）*计损充电度数
     */
    @ProtocolField(
            order = 17,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "已充金额")
    private Long chargedAmount;

    /**
     * 硬件故障 (2字节)
     * Bit位表示（0否1是），低位到高位顺序
     * Bit1：急停按钮动作故障；Bit2：无可用整流模块；Bit3：出风口温度过高；
     * Bit4：交流防雷故障；Bit5：交直流模块DC20通信中断；Bit6：绝缘检测模块FC08通信中断；
     * Bit7：电度表通信中断；Bit8：读卡器通信中断；Bit9：RC10通信中断；
     * Bit10：风扇调速板故障；Bit11：直流熔断器故障；Bit12：高压接触器故障；Bit13：门打开；
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
