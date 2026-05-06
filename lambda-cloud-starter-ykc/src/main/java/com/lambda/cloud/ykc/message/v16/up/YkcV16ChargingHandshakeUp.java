package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 充电握手（BMS 信息上送）
 * 对应协议帧类型 0x15（充电桩->平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x15", name = "充电握手", description = "充电握手阶段，BMS基础信息上送", version = "1.6")
public class YkcV16ChargingHandshakeUp {

    /**
     * 交易流水号 (16字节) BCD
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节) BCD
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节) BCD
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * BMS通信协议版本号 (3字节) BIN
     */
    @ProtocolField(order = 4, length = 3, computed = true, dataType = ProtocolDataType.HEX, description = "BMS通信协议版本号")
    private String bmsProtocolVersion;

    /**
     * 电池类型 (1字节) BIN
     */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池类型")
    private Integer batteryType;

    /**
     * 额定容量 (2字节) BIN，0.1Ah/bit
     */
    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "额定容量Ah")
    private Integer ratedCapacity;

    /**
     * 额定总电压 (2字节) BIN，0.1V/bit
     */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            precision = 1,
            description = "额定总电压V")
    private Integer ratedTotalVoltage;

    /**
     * 厂商名称 (4字节) BIN（ASCII）
     */
    @ProtocolField(order = 8, length = 4, computed = true, dataType = ProtocolDataType.ASCII, description = "厂商名称")
    private String manufacturerName;

    /**
     * 电池序号 (4字节) BIN
     */
    @ProtocolField(order = 9, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "电池序号")
    private String batterySequence;

    /**
     * 电池组生产日期-年 (1字节) BIN，1985年偏移量，数据范围：1985~2235年
     */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "电池组生产日期-年")
    private Integer productionYear;

    /**
     * 电池组生产日期-月 (1字节) BIN，0月偏移量，数据范围：1~12月
     */
    @ProtocolField(
            order = 11,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "电池组生产日期-月")
    private Integer productionMonth;

    /**
     * 电池组生产日期-日 (1字节) BIN，0日偏移量，数据范围：1~31日
     */
    @ProtocolField(
            order = 12,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "电池组生产日期-日")
    private Integer productionDay;

    /**
     * 电池组充电次数 (3字节) BIN，1次/位，0次偏移量
     */
    @ProtocolField(
            order = 13,
            length = 3,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "电池组充电次数")
    private Integer chargeCycleCount;

    /**
     * 电池组产权标识 (1字节) BIN：0=租赁；1=车自有
     */
    @ProtocolField(order = 14, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "电池组产权标识")
    private Integer batteryOwner;

    /**
     * 预留位 (1字节)
     */
    @ProtocolField(order = 15, length = 1, computed = true, dataType = ProtocolDataType.HEX, description = "预留位")
    private String reserved;

    /**
     * BMS车辆识别码 (17字节) BIN，VIN码
     */
    @ProtocolField(
            order = 16,
            length = 17,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            description = "BMS车辆识别码VIN")
    private String vinCode;

    /**
     * BMS软件版本号 (8字节) BIN
     */
    @ProtocolField(order = 17, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "BMS软件版本号")
    private String bmsSoftwareVersion;
}
