package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 余额更新应答
 * <p>
 * 帧类型码：0x41
 * 对应协议文档：8.10 余额更新应答
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：桩接收到平台余额更新后的应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "41", name = "余额更新应答", description = "桩接收平台余额更新后的应答")
public class YkcV20AccountBalanceUpdateReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 物理卡号 (8字节)
     * BIN码，不足8位补零，非必填
     * 有物理卡号时桩需要校验当前充电是否此卡充电，没有物理卡号则直接更新当前充电用户余额
     */
    @ProtocolField(order = 2, length = 8, optional = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 修改结果 (1字节)
     * 0x00-修改成功
     * 0x01-设备编号错误
     * 0x02-卡号错误
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "修改结果")
    private Integer updateResult;
}
