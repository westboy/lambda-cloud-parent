package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 对时设置应答
 * <p>
 * 帧类型码：0x55
 * 对应协议文档：9.4 对时设置应答
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电桩接收到运营平台同步充电桩时钟时应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "55", name = "对时设置应答", description = "桩在收到平台下发对时设置帧后回复")
public class YkcV20TimeSyncReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 当前时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(
            order = 2,
            length = 7,
            dataType = ProtocolDataType.CP56TIME2A,
            littleEndian = true,
            description = "当前时间")
    private String currentTime;
}
