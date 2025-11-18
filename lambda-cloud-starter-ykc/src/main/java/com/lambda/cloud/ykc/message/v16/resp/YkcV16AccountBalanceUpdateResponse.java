package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 8.10 余额更新应答
 * 对应协议帧类型 0x41
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x41", name = "余额更新应答", description = "桩接收平台余额更新后的应答", version = "1.6")
public class YkcV16AccountBalanceUpdateResponse {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 物理卡号 (8字节) BIN码，不足8位补零，非必填 */
    @ProtocolField(
            order = 2,
            length = 8,
            computed = true,
            optional = true,
            dataType = ProtocolDataType.HEX,
            description = "物理卡号")
    private String physicalCardNumber;

    /** 修改结果 (1字节) BIN码 */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "修改结果")
    private Integer updateResult;
}
