package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 上传实时监测数据响应详细信息
 * <p>
 * 帧类型码：0x13
 * 对应协议文档：7.2 上传实时监测数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "13", name = "上传实时监测数据", description = "上传实时监测数据响应详细信息")
public class YkcV20UploadRealtimeDataRequestDetail {

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
     * 状态 (1字节)
     * 0x00：离线，0x01：故障，0x02：空闲，0x03：充电
     * 需做到变位上送
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "状态")
    private Integer status;

    /**
     * 枪是否归位 (1字节)
     * 0x00 否，0x01 是，0x02 未知（无法检测到枪是否插回枪座即未知）
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "枪是否归位")
    private Integer gunInPosition;

    /**
     * 是否插枪 (1字节)
     * 0x00 否，0x01 是
     * 需做到变位上送
     */
    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "是否插枪")
    private Integer gunPlugged;

    /**
     * 输出电压 (2字节)
     * 精确到小数点后一位；待机置零
     */
    @ProtocolField(order = 7, length = 2, dataType = ProtocolDataType.UINT16, description = "输出电压")
    private Integer outputVoltage;

    /**
     * 输出电流 (2字节)
     * 精确到小数点后一位；待机置零
     */
    @ProtocolField(order = 8, length = 2, dataType = ProtocolDataType.UINT16, description = "输出电流")
    private Integer outputCurrent;

    /**
     * 枪线温度 (1字节)
     * 整形，偏移量-50；待机置零
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "枪线温度")
    private Integer gunCableTemperature;

    /**
     * 枪线编码 (8字节)
     * 没有置零
     */
    @ProtocolField(order = 10, length = 8, dataType = ProtocolDataType.HEX, description = "枪线编码")
    private String gunCableCode;

    /**
     * SOC (1字节)
     * 待机置零；交流桩置零
     */
    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "SOC")
    private Integer soc;

    /**
     * 电池组最高温度 (1字节)
     * 整形，偏移量-50ºC；待机置零；交流桩置零
     */
    @ProtocolField(order = 12, length = 1, dataType = ProtocolDataType.UINT8, description = "电池组最高温度")
    private Integer batteryMaxTemperature;

    /**
     * 累计充电时间 (2字节)
     * 单位：min；待机置零
     */
    @ProtocolField(order = 13, length = 2, dataType = ProtocolDataType.UINT16, description = "累计充电时间")
    private Integer accumulatedChargingTime;

    /**
     * 剩余时间 (2字节)
     * 单位：min；待机置零、交流桩置零
     */
    @ProtocolField(order = 14, length = 2, dataType = ProtocolDataType.UINT16, description = "剩余时间")
    private Integer remainingTime;

    /**
     * 充电度数 (4字节)
     * 精确到小数点后四位；待机置零
     */
    @ProtocolField(order = 15, length = 4, dataType = ProtocolDataType.UINT32, description = "充电度数")
    private Integer chargingEnergy;

    /**
     * 计损充电度数 (4字节)
     * 精确到小数点后四位；待机置零
     * 未设置计损比例时等于充电度数
     */
    @ProtocolField(order = 16, length = 4, dataType = ProtocolDataType.UINT32, description = "计损充电度数")
    private Integer lossAdjustedChargingEnergy;

    /**
     * 已充金额 (4字节)
     * 精确到小数点后四位；待机置零
     * （电费+服务费）*计损充电度数
     */
    @ProtocolField(order = 17, length = 4, dataType = ProtocolDataType.UINT32, description = "已充金额")
    private Integer chargedAmount;

    /**
     * 硬件故障 (2字节)
     * Bit位表示（0否1是），低位到高位顺序
     */
    @ProtocolField(order = 18, length = 2, dataType = ProtocolDataType.UINT16, description = "硬件故障")
    private Integer hardwareFault;

    /**
     * 桩体温度 (1字节)
     * 整形，偏移量-50ºC；交流桩置零
     */
    @ProtocolField(order = 19, length = 1, dataType = ProtocolDataType.UINT8, description = "桩体温度")
    private Integer pileTemperature;

    /**
     * 烟感状态 (1字节)
     * 0x01：启动，0x02：空闲，0x03：报警，0x04：故障
     * 取不到置零
     */
    @ProtocolField(order = 20, length = 1, dataType = ProtocolDataType.UINT8, description = "烟感状态")
    private Integer smokeDetectorStatus;

    /**
     * 电表示值 (5字节)
     * 精确到小数点后四位，取不到置零
     */
    @ProtocolField(order = 21, length = 5, dataType = ProtocolDataType.HEX, description = "电表示值")
    private String meterReading;
}
