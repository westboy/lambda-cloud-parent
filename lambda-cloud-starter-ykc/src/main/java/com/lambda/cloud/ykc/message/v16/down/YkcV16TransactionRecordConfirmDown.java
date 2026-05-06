package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议交易记录确认消息体
 * <p>
 * 对应协议帧类型 0x40，平台确认接收交易记录
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x40", name = "交易记录确认", description = "平台确认接收交易记录", version = "1.6")
public class YkcV16TransactionRecordConfirmDown {

    /** 交易流水号 (16字节) BCD码 */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /** 确认结果 (1字节) BIN码: 0x00 上传成功, 0x01 非法账单 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
