package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.0协议充电握手响应详细信息
 * <p>
 * 对应协议帧类型 0x15，GBT-27930充电桩与BMS充电握手阶段报文
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "15", name = "充电握手", description = "GBT-27930充电桩与BMS充电握手阶段报文", version = "2.0")
public class YkcV20ChargingHandshakeRequestDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码，见名词解释
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

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
    private String connectorId;

    /**
     * BMS通信协议版本号 (3字节)
     * BIN码，当前版本为V1.1，表示为：byte3，byte2—0001H；byte1—01H
     */
    @ProtocolField(
            order = 4,
            length = 3,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            description = "BMS通信协议版本号")
    private byte[] bmsProtocolVersion;

    /**
     * BMS电池类型 (1字节)
     * BIN码，电池类型：01H：铅酸电池；02H：氢电池；03H：磷酸铁锂电池；04H：锰酸锂电池；05H：钴酸锂电池；06H：三元材料电池；07H：聚合物锂离子电池；08H：钛酸锂电池；FFH：其他
     */
    @ProtocolField(order = 5, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "BMS电池类型")
    private Integer bmsBatteryType;

    /**
     * BMS整车动力蓄电池系统额定容量 (2字节)
     * BIN码，0.1Ah/位，0Ah偏移量
     */
    @ProtocolField(
            order = 6,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS整车动力蓄电池系统额定容量")
    private Integer bmsRatedCapacity;

    /**
     * BMS整车动力蓄电池系统额定总电压 (2字节)
     * BIN码，0.1V/位，0V偏移量
     */
    @ProtocolField(
            order = 7,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            description = "BMS整车动力蓄电池系统额定总电压")
    private Integer bmsRatedVoltage;

    /**
     * BMS电池生产厂商名称 (4字节)
     * BIN码，标准ASCII码
     */
    @ProtocolField(
            order = 8,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            description = "BMS电池生产厂商名称")
    private byte[] bmsManufacturerName;

    /**
     * BMS电池组序号 (4字节)
     * BIN码，预留，由厂商自行定义
     */
    @ProtocolField(order = 9, length = 4, computed = true, dataType = ProtocolDataType.UINT32, description = "BMS电池组序号")
    private byte[] bmsBatteryPackSerialNumber;

    /**
     * BMS电池组生产日期年 (1字节)
     * BIN码，1985年偏移量，数据范围：1985～2235年
     */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "BMS电池组生产日期年")
    private Integer bmsProductionYear;

    /**
     * BMS电池组生产日期月 (1字节)
     * BIN码，0月偏移量，数据范围：1～12月
     */
    @ProtocolField(
            order = 11,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "BMS电池组生产日期月")
    private Integer bmsProductionMonth;

    /**
     * BMS电池组生产日期日 (1字节)
     * BIN码，0日偏移量，数据范围：1～31日
     */
    @ProtocolField(
            order = 12,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "BMS电池组生产日期日")
    private Integer bmsProductionDay;

    /**
     * BMS电池组充电次数 (3字节)
     * BIN码，1次/位，0次偏移量
     */
    @ProtocolField(
            order = 13,
            length = 3,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            description = "BMS电池组充电次数")
    private byte[] bmsChargingCycles;

    /**
     * BMS电池组产权标识 (1字节)
     * BIN码，00H：电池租赁；01H：电池购买；FFH：未知
     */
    @ProtocolField(
            order = 14,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "BMS电池组产权标识")
    private Integer bmsOwnershipIdentifier;

    /**
     * 预留位 (1字节)
     * BIN码，预留，置0
     */
    @ProtocolField(order = 15, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "预留位")
    private Integer reserved1;

    /**
     * BMS车辆识别码 (17字节)
     * ASCII码，车辆识别码VIN
     */
    @ProtocolField(
            order = 16,
            length = 17,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            description = "BMS车辆识别码")
    private String bmsVinCode;

    /**
     * BMS软件版本号 (8字节)
     * BIN码，预留，由厂商自行定义
     */
    @ProtocolField(order = 17, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "BMS软件版本号")
    private byte[] bmsSoftwareVersion;
}
