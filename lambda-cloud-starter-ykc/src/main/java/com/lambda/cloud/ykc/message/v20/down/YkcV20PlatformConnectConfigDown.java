package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 平台连接设置
 * <p>
 * 帧类型码：0x5D
 * 对应协议文档：9.11 平台连接设置（可选）
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台下发连接信息到桩，桩根据下发参数修改配置
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "5D", name = "平台连接设置", description = "平台连接设置命令")
public class YkcV20PlatformConnectConfigDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 平台域名 (64字节)
     * ASCII码，不足64位补零
     */
    @ProtocolField(order = 2, length = 64, dataType = ProtocolDataType.ASCII, description = "平台域名")
    private String platformDomain;

    /**
     * 平台地址 (15字节)
     * ASCII码，不足15位补零，IPV4地址
     */
    @ProtocolField(order = 3, length = 15, dataType = ProtocolDataType.ASCII, description = "平台地址")
    private String platformAddress;

    /**
     * 平台端口 (2字节)
     * BIN码，没有就置0
     */
    @ProtocolField(order = 4, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "平台端口")
    private Integer platformPort;
}