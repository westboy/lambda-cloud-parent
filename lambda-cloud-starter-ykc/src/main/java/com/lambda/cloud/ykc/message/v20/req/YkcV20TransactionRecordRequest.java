package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20HalfHourEnergy;
import com.lambda.cloud.ykc.message.v20.model.YkcV20TransactionRatePeriod;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "3D", name = "交易记录", description = "交易记录响应详细信息")
public class YkcV20TransactionRecordRequest {

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(
            order = 4,
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "开始时间")
    private LocalDateTime startTime;

    @ProtocolField(
            order = 5,
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "结束时间")
    private LocalDateTime endTime;

    @ProtocolField(order = 6, length = 6, dataType = ProtocolDataType.BCD, description = "电表表号")
    private String meterNo;

    @ProtocolField(order = 7, length = 34, dataType = ProtocolDataType.HEX, description = "电表密文")
    private String meterCipher;

    @ProtocolField(order = 8, length = 2, dataType = ProtocolDataType.HEX, description = "电表协议版本号")
    private String meterProtocolVersion;

    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "加密方式")
    private Integer encryptMethod;

    @ProtocolField(order = 10, length = 5, dataType = ProtocolDataType.HEX, littleEndian = true, description = "电表总起值")
    private String meterStartValue;

    @ProtocolField(order = 11, length = 5, dataType = ProtocolDataType.HEX, littleEndian = true, description = "电表总止值")
    private String meterEndValue;

    @ProtocolField(
            order = 12,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "总电量")
    private Integer totalEnergy;

    @ProtocolField(
            order = 13,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "计损总电量")
    private Integer totalEnergyWithLoss;

    @ProtocolField(
            order = 14,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "消费金额")
    private Integer totalAmount;

    @ProtocolField(order = 15, length = 17, dataType = ProtocolDataType.ASCII, description = "电动汽车唯一标识")
    private String vin;

    @ProtocolField(order = 16, length = 1, dataType = ProtocolDataType.UINT8, description = "交易标识")
    private Integer transactionType;

    @ProtocolField(
            order = 17,
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "交易日期、时间")
    private LocalDateTime transactionDateTime;

    @ProtocolField(order = 18, length = 1, dataType = ProtocolDataType.UINT8, description = "停止原因")
    private Integer stopReason;

    @ProtocolField(order = 19, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    @ProtocolField(order = 20, length = 1, dataType = ProtocolDataType.UINT8, description = "费率时段数量")
    private Integer ratePeriodCount;

    @ProtocolField(
            order = 21,
            length = 16,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20TransactionRatePeriod.class,
            listElementSizeField = "ratePeriodCount",
            description = "费率时段列表")
    private List<YkcV20TransactionRatePeriod> ratePeriods;

    @ProtocolField(
            order = 22,
            length = 4,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20HalfHourEnergy.class,
            listElementSize = 48,
            description = "半小时电量列表")
    private List<YkcV20HalfHourEnergy> halfHourEnergies;
}
