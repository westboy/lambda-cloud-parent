package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程重启应答
 * <p>
 * 帧类型码：0x91
 * 对应协议文档：11.2 远程重启应答
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电桩接收到运营平台远程重启指令时，响应本数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "91", name = "远程重启应答", description = "桩重启前回复平台")
public class YkcV20RemoteRebootReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 设置结果 (1字节)
     * 0x00失败
     * 0x01成功
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "重启结果")
    private Integer result;
}
