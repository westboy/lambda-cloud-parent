package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 12.3 运营平台远程控制并充启机（下行）
 * 对应协议帧类型 0xA4
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xA4", name = "远程控制并充启机", description = "远程启动充电并为并充模式时下发本命令", version = "1.6")
public class YkcV16ParallelRemoteStartChargingRequest {

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

    /** 物理卡号 (8字节) BIN码，不足补零 */
    @ProtocolField(order = 5, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /** 账户余额 (4字节) BIN码，两位小数 */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            description = "账户余额")
    private Long accountBalance;

    /** 并充序号 (6字节) BCD码，平台生成：年月日时分秒（多个枪一致） */
    @ProtocolField(order = 7, length = 6, computed = true, dataType = ProtocolDataType.BCD, description = "并充序号")
    private String parallelSequence;
}
