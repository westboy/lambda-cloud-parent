package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 功率修改
 * <p>
 * 帧类型码：0x52
 * 对应协议文档：9.1 功率修改
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：远程设置充电桩终端充电输出最大功率
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "52", name = "功率修改", description = "功率修改命令")
public class YkcV20PowerModifyDown {

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
     * 允许最大功率 (2字节)
     * BIN码，单位：kW
     */
    @ProtocolField(
            order = 3,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "允许最大功率")
    private Integer maxPower;

    /**
     * 指令响应优先级 (1字节)
     * BIN码，数字越大优先级越高
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "指令响应优先级")
    private Integer priority;

    /**
     * 限制时间 (2字节)
     * BIN码，单位：分钟
     * 注意：只对本次充电有效，限制时间内重新发起充电不受限制
     */
    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "限制时间")
    private Integer limitTime;
}
