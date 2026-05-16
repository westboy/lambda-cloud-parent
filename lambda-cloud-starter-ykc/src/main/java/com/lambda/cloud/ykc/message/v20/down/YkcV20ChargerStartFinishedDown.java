package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电机启动完成应答
 * <p>
 * 帧类型码：0x4E
 * 对应协议文档：8.25 充电机启动完成
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台对充电机启动完成的应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4E", name = "充电机启动完成应答", description = "平台对充电机启动完成的应答")
public class YkcV20ChargerStartFinishedDown {

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
     * 确认结果 (1字节)
     * BIN码
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
