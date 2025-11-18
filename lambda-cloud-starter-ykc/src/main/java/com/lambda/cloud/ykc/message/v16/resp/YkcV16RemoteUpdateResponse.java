package com.lambda.cloud.ykc.message.v16.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 11.4 远程更新应答（上行）
 * 对应协议帧类型 0x93
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x93", name = "远程更新应答", description = "充电桩对远程更新指令的执行状态上送", version = "1.6")
public class YkcV16RemoteUpdateResponse {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 升级状态 (1字节) BIN码：0x00成功；0x01编号错误；0x02程序与桩型号不符；0x03下载更新文件超时 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "升级状态")
    private Integer updateStatus;
}
