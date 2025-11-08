package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录确认请求详细信息
 * <p>
 * 帧类型码：0x40
 * 对应协议文档：8.8 交易记录确认
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "40", name = "交易记录确认", description = "交易记录确认请求详细信息")
public class YkcV20TransactionConfirmRequest {

    /**
     * 交易流水号 (16字节)
     * 见名词解释
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 确认结果 (1字节)
     * 0x00上传成功 0x01非法账单
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
