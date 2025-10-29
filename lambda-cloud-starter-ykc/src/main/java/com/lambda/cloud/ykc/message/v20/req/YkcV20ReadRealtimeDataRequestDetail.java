package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 读取实时监测数据请求详细信息
 * <p>
 * 帧类型码：0x12
 * 对应协议文档：7.1 读取实时监测数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "12", name = "读取实时监测数据", description = "读取实时监测数据请求详细信息")
public class YkcV20ReadRealtimeDataRequestDetail {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;
}
