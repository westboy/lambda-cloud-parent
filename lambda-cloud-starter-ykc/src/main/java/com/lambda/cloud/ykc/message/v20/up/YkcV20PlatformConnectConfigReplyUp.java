package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 平台连接设置应答
 * <p>
 * 帧类型码：0x5C
 * 对应协议文档：9.12 平台连接设置应答（可选）
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电桩在参数设置后进行应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "5C", name = "平台连接设置应答", description = "桩在收到平台连接设置帧后回复")
public class YkcV20PlatformConnectConfigReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 设置结果 (1字节)
     * 0x00失败
     * 0x01成功
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "修改结果")
    private Integer result;
}
