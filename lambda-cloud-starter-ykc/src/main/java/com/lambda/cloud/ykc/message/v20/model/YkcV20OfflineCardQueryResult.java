package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡查询结果
 * <p>
 * 对应协议文档：8.15 离线卡数据查询应答
 * 功能：用于描述每张离线卡的查询结果
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "离线卡查询结果")
public class YkcV20OfflineCardQueryResult {

    /**
     * 物理卡号 (8字节)
     * BIN码
     */
    @ProtocolField(order = 1, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNo;

    /**
     * 查询结果 (1字节)
     * 0x00-无效
     * 0x01-有效
     * 0x02-正在使用
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "查询结果")
    private Integer queryResult;
}
