package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 充电握手（BMS 信息上送）
 * 对应协议帧类型 0x15
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x15", name = "充电握手", description = "充电握手阶段，BMS基础信息上送", version = "1.6")
public class YkcV16ChargingHandshakeResponse {

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
     * 电池组序号 (4字节) BIN
     */
    @ProtocolField(order = 9, length = 4, computed = true, dataType = ProtocolDataType.HEX, description = "电池组序号")
    private String batteryPackSerial;

    /** 年 (1字节) */
    @ProtocolField(order = 10, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "年")
    private Integer year;

    /** 月 (1字节) */
    @ProtocolField(order = 11, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "月")
    private Integer month;

    /** 日 (1字节) */
    @ProtocolField(order = 12, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "日")
    private Integer day;

    /**
     * 充电次数 (3字节) BIN
     */
    @ProtocolField(order = 13, length = 3, computed = true, dataType = ProtocolDataType.HEX, description = "充电次数")
    private String chargeCount;

    /** 产权标识 (1字节) */
    @ProtocolField(order = 14, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "产权标识")
    private Integer ownershipTag;

    /** 预留位 (1字节) */
    @ProtocolField(order = 15, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved;

    /** VIN (17字节) ASCII */
    @ProtocolField(order = 16, length = 17, computed = true, dataType = ProtocolDataType.ASCII, description = "VIN")
    private String vin;

    /** 软件版本号 (8字节) BIN */
    @ProtocolField(order = 17, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "软件版本号")
    private String softwareVersion;
}
