package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议计费模型验证请求应答消息体
 * <p>
 * 对应协议帧类型 0x06，运营平台对计费模型验证请求的应答
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x06", name = "计费模型验证应答", description = "计费模型验证请求应答消息体", version = "1.6")
public class YkcV16BillingModelVerificationResponse {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 计费模型编码 (2字节)
     */
    @ProtocolField(order = 2, length = 2, computed = true, dataType = ProtocolDataType.BCD, description = "计费模型编码")
    private String billingModelCode;

    /**
     * 验证结果 (1字节)
     * 0x00 桩计费模型与平台一致；0x01 不一致
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "验证结果")
    private Integer verificationResult;
}
