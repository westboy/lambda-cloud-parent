package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程账户余额更新
 * <p>
 * 帧类型码：0x42
 * 对应协议文档：8.9 远程账户余额更新
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台在用户完成充值后会将用户更新的余额下发到充电桩
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "42", name = "远程账户余额更新", description = "远程账户余额更新命令")
public class YkcV20AccountBalanceUpdateDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 物理卡号 (8字节)
     * BIN码，不足8位补零
     * 如果不为零，需要校验本次充电是否为此卡充电
     * 如果为零，则不校验，直接更新桩当前充电用户余额
     */
    @ProtocolField(order = 3, length = 8, optional = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 修改后账户金额 (4字节)
     * BIN码，保留两位小数
     */
    @ProtocolField(
            order = 4,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 2,
            description = "修改后账户金额")
    private Integer updatedBalance;
}
