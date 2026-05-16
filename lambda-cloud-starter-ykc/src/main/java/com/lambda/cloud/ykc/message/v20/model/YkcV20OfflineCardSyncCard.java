package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡信息
 * <p>
 * 对应协议文档：8.11 离线卡数据同步
 * 功能：用于描述离线卡的逻辑卡号和物理卡号信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "离线卡")
public class YkcV20OfflineCardSyncCard {

    /**
     * 逻辑卡号 (8字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNo;

    /**
     * 物理卡号 (8字节)
     * BIN码
     */
    @ProtocolField(order = 2, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;
}
