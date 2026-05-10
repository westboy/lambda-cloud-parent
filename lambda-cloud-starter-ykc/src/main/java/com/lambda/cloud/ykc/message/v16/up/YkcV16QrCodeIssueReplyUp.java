package com.lambda.cloud.ykc.message.v16.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 桩应答远程下发二维码前缀指令
 * <p>
 * 对应协议帧类型 0xF1（充电桩->运营平台）
 * </p>
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xF1", name = "桩应答远程下发二维码前缀指令", description = "桩应答远程下发二维码前缀指令", version = "1.6")
public class YkcV16QrCodeIssueReplyUp {

    /**
     * 桩编码 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

    /**
     * 下发结果 (1字节)
     * BIN码：0x00 成功，0x01 失败
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "下发结果")
    private Integer issueResult;
}
