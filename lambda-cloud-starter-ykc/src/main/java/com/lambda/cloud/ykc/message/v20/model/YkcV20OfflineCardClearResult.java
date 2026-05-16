package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡清除结果
 * <p>
 * 对应协议文档：8.14 离线卡清除应答
 * 功能：用于描述每张离线卡的清除结果
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "离线卡清除结果")
public class YkcV20OfflineCardClearResult {

    /**
     * 物理卡号 (8字节)
     * BIN码
     */
    @ProtocolField(order = 1, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;

    /**
     * 清除标记 (1字节)
     * 0x00-成功
     * 0x01-卡号错误
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "清除标记")
    private Integer clearFlag;

    /**
     * 失败原因 (1字节)
     * 0x00-无
     * 0x01-在充电中，无法清除
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;
}
