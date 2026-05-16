package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程重启
 * <p>
 * 帧类型码：0x92
 * 对应协议文档：11.1 远程重启
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：重启充电桩，应对部分问题，如卡死
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "92", name = "远程重启", description = "远程重启命令")
public class YkcV20RemoteRebootDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 执行控制 (1字节)
     * 0x01：立即执行
     * 0x02：空闲执行
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executionControl;
}
