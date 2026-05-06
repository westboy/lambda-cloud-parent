package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 读取实时监测数据
 * 对应协议帧类型 0x12（运营平台 -> 充电桩）
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x12", name = "读取实时监测数据", description = "读取实时监测数据请求", version = "1.6")
public class YkcV16ReadRealtimeDataDown {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;
}
