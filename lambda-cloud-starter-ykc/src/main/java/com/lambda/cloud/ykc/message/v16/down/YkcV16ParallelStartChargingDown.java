package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 12.2 运营平台确认并充启动充电（下行应答）
 * 对应协议帧类型 0xA2
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xA2", name = "确认并充启动充电", description = "启动充电鉴权结果并带并充序号", version = "1.6")
public class YkcV16ParallelStartChargingDown {

    /** 交易流水号 (16字节) BCD码 */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BCD码 */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 逻辑卡号 (8字节) BCD码，不足补零 */
    @ProtocolField(order = 4, length = 8, computed = true, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /** 账户余额 (4字节) BIN，保留两位小数 */
    @ProtocolField(
            order = 5,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 2,
            description = "账户余额")
    private BigDecimal accountBalance;

    /** 鉴权成功标志 (1字节) BIN：0x00失败；0x01成功 */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "鉴权成功标志")
    private Integer authResult;

    /** 失败原因 (1字节) BCD码 */
    @ProtocolField(order = 7, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "失败原因")
    private Integer failureReason;

    /** 并充序号 (6字节) BCD码，桩上送的并充序号 */
    @ProtocolField(order = 8, length = 6, computed = true, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}
