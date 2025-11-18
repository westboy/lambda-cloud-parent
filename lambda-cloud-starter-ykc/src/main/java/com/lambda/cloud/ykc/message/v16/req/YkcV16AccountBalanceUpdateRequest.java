package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 8.9 远程账户余额更新
 * 对应协议帧类型 0x42
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x42", name = "远程账户余额更新", description = "平台在用户完成充值后下发更新余额到充电桩", version = "1.6")
public class YkcV16AccountBalanceUpdateRequest {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BCD码 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 物理卡号 (8字节) BIN码，不足8位补零 */
    @ProtocolField(order = 3, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /** 修改后账户金额 (4字节) BIN码，保留两位小数 */
    @ProtocolField(
            order = 4,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            description = "修改后账户金额")
    private Long updatedAccountBalance;
}
