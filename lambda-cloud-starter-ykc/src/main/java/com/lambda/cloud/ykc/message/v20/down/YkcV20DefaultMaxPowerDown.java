package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 默认最大功率下发
 * <p>
 * 帧类型码：0x60
 * 对应协议文档：9.7 默认最大功率下发
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：运营平台向桩下发默认最大功率限制
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "60", name = "默认最大功率下发", description = "默认最大功率下发命令")
public class YkcV20DefaultMaxPowerDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 默认最大功率 (2字节)
     * BIN码，单位：kW
     */
    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "默认最大功率")
    private Integer defaultMaxPower;

    /**
     * 开始时间 (7字节)
     * CP56Time2a格式，到达此时间后按最大功率执行
     */
    @ProtocolField(order = 4, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "开始时间")
    private String startTime;

    /**
     * 结束时间 (7字节)
     * CP56Time2a格式，到达此时间后解除最大功率执行限制
     */
    @ProtocolField(order = 5, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "结束时间")
    private String endTime;
}