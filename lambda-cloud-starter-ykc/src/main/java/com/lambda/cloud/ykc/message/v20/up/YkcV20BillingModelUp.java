package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩计费模型请求
 * <p>
 * 帧类型码：0x09
 * 对应协议文档：6.7 充电桩计费模型请求
 * 数据传输方向：充电桩 → 运营平台（上行）
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "09", name = "充电桩计费模型请求", description = "充电桩计费模型请求详细信息")
public class YkcV20BillingModelUp {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;
}
