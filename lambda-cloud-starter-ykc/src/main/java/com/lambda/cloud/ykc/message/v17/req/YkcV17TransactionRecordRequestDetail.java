package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议交易记录消息体
 * <p>
 * 对应协议帧类型 0x3D，充电桩在网络正常情况下，主动发送结算账单
 * </p>
 *
 * @author Jin
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x3D", name = "交易记录", description = "充电桩在网络正常情况下，主动发送结算账单", version = "1.7")
public class YkcV17TransactionRecordRequestDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码
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
     * 开始时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(
            order = 4,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "开始时间")
    private byte[] startTime;

    /**
     * 结束时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(
            order = 5,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "结束时间")
    private byte[] endTime;

    /**
     * 电表表号 (6字节)
     * BCD码
     */
    @ProtocolField(order = 6, length = 6, computed = true, dataType = ProtocolDataType.BCD, description = "电表表号")
    private String meterNumber;

    /**
     * 电表密文 (34字节)
     * BIN码
     */
    @ProtocolField(order = 7, length = 34, computed = true, dataType = ProtocolDataType.HEX, description = "电表密文")
    private String meterCiphertext;

    /**
     * 电表协议版本号 (2字节)
     * BIN码
     */
    @ProtocolField(
            order = 8,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "电表协议版本号")
    private Integer meterProtocolVersion;

    /**
     * 加密方式 (1字节)
     * BIN码
     */
    @ProtocolField(order = 9, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "加密方式")
    private Integer encryptionMethod;

    /**
     * 尖单价 (4字节)
     * 精确到小数点后五位（尖电费+尖服务费）
     */
    @ProtocolField(
            order = 10,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "尖单价")
    private BigDecimal peakPrice;

    /**
     * 尖电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 11,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "尖电量")
    private BigDecimal peakEnergy;

    /**
     * 尖损耗电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 12,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "尖损耗电量")
    private BigDecimal peakEnergyWithLoss;

    /**
     * 尖金额 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 13,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "尖金额")
    private BigDecimal peakAmount;

    /**
     * 高单价 (4字节)
     * 精确到小数点后五位（高电费+高服务费）
     */
    @ProtocolField(
            order = 14,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "高单价")
    private BigDecimal highPrice;

    /**
     * 高电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 15,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "高电量")
    private BigDecimal highEnergy;

    /**
     * 高损耗电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 16,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "高损耗电量")
    private BigDecimal highEnergyWithLoss;

    /**
     * 高金额 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 17,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "高金额")
    private BigDecimal highAmount;

    /**
     * 平单价 (4字节)
     * 精确到小数点后五位（平电费+平服务费）
     */
    @ProtocolField(
            order = 18,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "平单价")
    private BigDecimal normalPrice;

    /**
     * 平电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 19,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "平电量")
    private BigDecimal normalEnergy;

    /**
     * 平损耗电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 20,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "平损耗电量")
    private BigDecimal normalEnergyWithLoss;

    /**
     * 平金额 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 21,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "平金额")
    private BigDecimal normalAmount;

    /**
     * 谷单价 (4字节)
     * 精确到小数点后五位（谷电费+谷服务费）
     */
    @ProtocolField(
            order = 22,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "谷单价")
    private BigDecimal valleyPrice;

    /**
     * 谷电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 23,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "谷电量")
    private BigDecimal valleyEnergy;

    /**
     * 谷损耗电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 24,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "谷损耗电量")
    private BigDecimal valleyEnergyWithLoss;

    /**
     * 谷金额 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 25,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "谷金额")
    private BigDecimal valleyAmount;

    /**
     * 电表总起值 (5字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 26,
            length = 5,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "电表总起值")
    private String meterStartValue;

    /**
     * 电表总止值 (5字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 27,
            length = 5,
            computed = true,
            dataType = ProtocolDataType.HEX,
            littleEndian = true,
            description = "电表总止值")
    private String meterEndValue;

    /**
     * 总电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 28,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "总电量")
    private BigDecimal totalEnergy;

    /**
     * 总损耗电量 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 29,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "总损耗电量")
    private BigDecimal totalEnergyWithLoss;

    /**
     * 总金额 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(
            order = 30,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "总金额")
    private BigDecimal totalAmount;

    /**
     * 电动汽车唯一标识 (17字节)
     * ASCII码，VIN码，正序直接上传，无需补0和反序
     */
    @ProtocolField(
            order = 31,
            length = 17,
            computed = true,
            dataType = ProtocolDataType.ASCII,
            description = "电动汽车唯一标识")
    private String vinCode;

    /**
     * 交易标识 (1字节)
     * BIN码: 0x01 app启动, 0x02 卡启动, 0x04 离线卡启动, 0x05 vin码启动充电
     */
    @ProtocolField(order = 32, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "交易标识")
    private Integer transactionType;

    /**
     * 交易日期、时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(
            order = 33,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "交易日期、时间")
    private byte[] transactionDateTime;

    /**
     * 停止原因 (1字节)
     * BIN码，见附录11.1
     */
    @ProtocolField(order = 34, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "停止原因")
    private Integer stopReason;

    /**
     * 物理卡号 (8字节)
     * BCD码，不足8位补0
     */
    @ProtocolField(order = 35, length = 8, computed = true, dataType = ProtocolDataType.BCD, description = "物理卡号")
    private String physicalCardNumber;
}
