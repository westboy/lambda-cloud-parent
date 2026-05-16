package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 密钥更新
 * <p>
 * 帧类型码：0x96
 * 对应协议文档：11.5 密钥更新
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：对桩进行RSA私钥下发，桩收到指令后更新密钥
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "96", name = "密钥更新", description = "密钥更新命令")
public class YkcV20KeyUpdateDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 密钥长度 (1字节)
     * BIN码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "密钥长度")
    private Integer keyLength;

    /**
     * 最新密钥 (N字节)
     * ASCII码，RSA配置公钥
     */
    @ProtocolField(order = 3, length = 88, dataType = ProtocolDataType.ASCII, description = "最新密钥")
    private String latestKey;

    /**
     * 执行控制 (1字节)
     * 0x01：立即执行
     * 0x02：空闲执行，默认
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executionControl;
}
