package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程停机命令回复响应详细信息
 * <p>
 * 帧类型码：0x35
 * 对应协议文档：8.6 远程停机命令回复
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "35", name = "远程停机命令回复", description = "远程停机命令回复响应详细信息")
public class YkcV20RemoteStopReplyResponseDetail {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 停止结果 (1字节)
     * 0x00失败 0x01成功
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "停止结果")
    private String stopResult;

    /**
     * 失败原因 (1字节)
     * 0x00无 0x01设备编号不匹配 0x02枪未处于充电状态 0x03其他
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
