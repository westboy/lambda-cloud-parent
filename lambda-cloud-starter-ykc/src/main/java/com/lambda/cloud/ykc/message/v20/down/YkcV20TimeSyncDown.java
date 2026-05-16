package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 对时设置
 * <p>
 * 帧类型码：0x56
 * 对应协议文档：9.3 对时设置
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：运营平台同步充电桩时钟，以保证充电桩与运营平台的时钟一致
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "56", name = "对时设置", description = "对时设置命令")
public class YkcV20TimeSyncDown {

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
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "对时时间")
    private String syncTime;
}
