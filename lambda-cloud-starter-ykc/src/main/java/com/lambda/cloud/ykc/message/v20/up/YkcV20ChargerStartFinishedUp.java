package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电机启动完成
 * <p>
 * 帧类型码：0x4F
 * 对应协议文档：8.25 充电机启动完成
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电设备在完成充电启动后，向平台主动发送启动结果及启动过程相关电池参数
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4F", name = "充电机启动完成", description = "充电设备完成启动后上送启动结果与部分电池参数")
public class YkcV20ChargerStartFinishedUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 启动结果 (1字节)
     * 0x00成功
     * 0x01失败
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "启动结果")
    private Integer startResult;

    /**
     * 失败原因 (2字节)
     * 0x0000成功，其他参考附录13.2
     */
    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "失败原因")
    private Integer failReason;

    /**
     * 当前电表总值 (5字节)
     * BIN码，精确到小数点后四位
     */
    @ProtocolField(order = 6, length = 5, dataType = ProtocolDataType.HEX, description = "当前电表总值")
    private String currentMeterValue;

    /**
     * 最大允许充电总电压 (2字节)
     * BIN码，0.1V/位，0V偏移量（BHM）
     */
    @ProtocolField(
            order = 7,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "最大允许充电总电压")
    private Integer maxAllowChargeVoltage;

    /**
     * BMS通信协议版本号 (3字节)
     * 当前版本为V1.1.2，表示为：0x01，0x01，0x02
     */
    @ProtocolField(order = 8, length = 3, dataType = ProtocolDataType.HEX, description = "BMS通信协议版本号")
    private String bmsProtocolVersion;

    /**
     * BMS电池类型 (1字节)
     * 0x01铅酸电池
     * 0x02氢电池
     * 0x03磷酸铁锂电池
     * 0x04锰酸锂电池
     * 0x05钴酸锂电池
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "BMS电池类型")
    private Integer batteryType;
}
