package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 余额更新应答
 * 对应协议帧类型 0x41（充电桩->运营平台）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x41", name = "余额更新应答", description = "远程账户余额更新应答", version = "1.6")
public class YkcV16AccountBalanceUpdateReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 物理卡号 (8字节)
     * BIN码，不足8位补零，非必填；有物理卡号时桩需要校验当前充电是否此卡充电，没有物理卡号则直接更新当前充电用户余额
     */
    @ProtocolField(order = 2, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 更新结果 (1字节)
     * BIN码: 0x00 成功, 0x01 失败
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "更新结果")
    private Integer updateResult;
}
