package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录召唤确认
 * <p>
 * 帧类型码：0x4C
 * 对应协议文档：8.24 交易记录召唤回复
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：桩应答平台发起的交易记录召唤请求
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "4C", name = "交易记录召唤确认", description = "桩对交易记录召唤的回复确认")
public class YkcV20TransactionRecordCallReplyUp {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 召唤结果 (1字节)
     * 0x00-成功
     * 0x01-失败
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "召唤结果")
    private Integer callResult;

    /**
     * 失败原因 (1字节)
     * 0x00-无
     * 0x01-无记录
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}
