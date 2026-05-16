package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电阶段BMS中止
 * <p>
 * 帧类型码：0x1D
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：BMS中止充电原因上送
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "1D", name = "充电阶段BMS中止", description = "BMS中止充电原因上送", version = "2.0")
public class YkcV20BmsAbortUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
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
     * 中止充电原因 (1字节)
     * BIN码
     */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "中止充电原因")
    private Integer abortReason;

    /**
     * 故障原因bit集合 (2字节)
     * BIN码
     */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "故障原因bit集合")
    private Integer faultReason;

    /**
     * 错误原因 (1字节)
     * BIN码
     */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "错误原因")
    private Integer errorReason;
}
