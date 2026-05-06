package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型验证请求应答详细信息
 * <p>
 * 帧类型码：0x06
 * 对应协议文档：6.6 计费模型验证请求应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "06", name = "计费模型验证请求应答", description = "计费模型验证请求应答详细信息")
public class YkcV20BillingModelVerificationResponse {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 计费模型编号 (2字节)
     */
    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;

    /**
     * 验证结果 (1字节)
     * 0x00 桩计费模型与平台一致
     * 0x01 桩计费模型与平台不一致
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "验证结果")
    private Integer verificationResult;
}
