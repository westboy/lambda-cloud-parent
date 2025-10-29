package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程启动充电命令回复响应详细信息
 * <p>
 * 帧类型码：0xA7
 * 对应协议文档：8.4 远程启动充电命令回复
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "A7", name = "远程启动充电命令回复", description = "远程启动充电命令回复响应详细信息")
public class YkcV20RemoteStartReplyResponseDetail {

    /**
     * 交易流水号 (16字节)
     * 见名词解释
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 启动结果 (1字节)
     * 0x00失败 0x01成功
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.BCD, description = "启动结果")
    private String startResult;

    /**
     * 失败原因 (1字节)
     * 0x00无 0x01设备编号不匹配 0x02枪已在充电 0x03设备故障 0x04设备离线 0x05未插枪
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failureReason;
}
