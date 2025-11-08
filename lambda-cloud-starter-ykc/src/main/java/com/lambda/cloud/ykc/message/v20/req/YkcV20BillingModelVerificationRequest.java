package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型验证请求详细信息
 * <p>
 * 帧类型码：0x05
 * 对应协议文档：6.5 计费模型验证请求
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "05", name = "计费模型验证请求", description = "计费模型验证请求详细信息")
public class YkcV20BillingModelVerificationRequest {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 计费模型编号 (2字节)
     * 首次连接到平台时置零
     */
    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;
}
