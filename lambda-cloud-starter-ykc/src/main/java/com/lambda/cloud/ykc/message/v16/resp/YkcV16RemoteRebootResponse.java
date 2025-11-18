package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 11.2 远程重启应答（上行）
 * 对应协议帧类型 0x91
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x91", name = "远程重启应答", description = "充电桩对远程重启的应答", version = "1.6")
public class YkcV16RemoteRebootResponse {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 设置结果 (1字节) BIN码：0x00 失败；0x01 成功 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "设置结果")
    private Integer setResult;
}
