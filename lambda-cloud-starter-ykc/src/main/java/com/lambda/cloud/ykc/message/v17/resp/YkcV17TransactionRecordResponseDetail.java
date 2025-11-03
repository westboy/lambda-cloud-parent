package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议交易记录确认消息体
 * <p>
 * 对应协议帧类型 0x40，运营平台确认收到充电桩上传的交易记录
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0x40", name = "交易记录确认", description = "运营平台确认收到充电桩上传的交易记录", version = "1.7")
public class YkcV17TransactionRecordResponseDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码，与0x3D报文中的交易流水号保持一致
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 确认结果 (1字节)
     * BIN码: 0x00 成功, 0x01 失败
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}
