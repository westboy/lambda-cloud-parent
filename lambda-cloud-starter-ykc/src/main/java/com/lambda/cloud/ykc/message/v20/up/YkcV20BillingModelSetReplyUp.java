package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型应答
 * <p>
 * 帧类型码：0x57
 * 对应协议文档：9.6 计费模型应答
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电桩接收到运营平台计费模型时，响应本数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "57", name = "计费模型应答", description = "桩保存后上送应答")
public class YkcV20BillingModelSetReplyUp {

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
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "设置结果")
    private Integer result;
}
