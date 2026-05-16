package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 参数设置
 * <p>
 * 帧类型码：0x5F
 * 对应协议文档：9.13 参数设置
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台下发参数配置信息到桩，桩根据下发参数修改配置
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "5F", name = "参数设置", description = "参数设置命令")
public class YkcV20WorkingParamsSetDown {

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
     * 即插即充开关 (1字节)
     * 0x00支持，默认
     * 0x01不支持
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "即插即充开关")
    private Integer plugAndChargeSwitch;

    /**
     * 鉴权超时时间 (1字节)
     * BIN码，单位：秒
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "鉴权超时时间")
    private Integer authTimeout;

    /**
     * 离线充电时间 (1字节)
     * BIN码，桩和平台离线后，达到配置时间，停止充电；单位：秒；默认0，不限制
     */
    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "离线充电时间")
    private Integer offlineChargingTime;
}
