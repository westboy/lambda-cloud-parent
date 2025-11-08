package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 BMS中止充电
 * 对应协议帧类型 0x1D
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x1D", name = "BMS中止", description = "BMS中止充电原因上送", version = "1.6")
public class YkcV16BmsAbortResponseDetail {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 中止充电原因 (1字节) */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "中止充电原因")
    private Integer abortReason;

    /** 中止充电故障原因 (2字节) bit集合 */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "故障原因bit集合")
    private Integer faultReason;

    /** 中止充电错误原因 (1字节) */
    @ProtocolField(order = 6, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "错误原因")
    private Integer errorReason;
}
