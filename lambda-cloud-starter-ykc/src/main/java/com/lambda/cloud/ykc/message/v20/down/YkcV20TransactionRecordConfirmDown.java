package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录确认
 * <p>
 * 帧类型码：0x40
 * 对应协议文档：8.8 交易记录确认
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：运营平台接收到结算账单上传后，回复此确认信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "40", name = "交易记录确认", description = "平台对交易记录上送的确认回复")
public class YkcV20TransactionRecordConfirmDown {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 确认结果 (1字节)
     * 0x00上传成功
     * 0x01非法账单
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "确认结果")
    private Integer confirmResult;
}