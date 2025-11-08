package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议计费模型请求消息体
 * <p>
 * 对应协议帧类型 0x09，充电桩计费模型请求
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x09", name = "计费模型请求", description = "充电桩计费模型请求消息体", version = "1.6")
public class YkcV16BillingModelRequest {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;
}
