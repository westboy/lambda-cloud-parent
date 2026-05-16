package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程更新应答
 * <p>
 * 帧类型码：0x93
 * 对应协议文档：11.4 远程更新应答
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：充电桩执行过运营平台远程更新指令，响应本数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "93", name = "远程更新应答", description = "桩在收到平台远程更新帧后回复")
public class YkcV20RemoteUpdateReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 升级状态 (1字节)
     * 0x00-成功
     * 0x01-编号错误
     * 0x02-程序与桩型号不符
     * 0x03-下载更新文件超时
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "更新结果")
    private Integer result;
}
