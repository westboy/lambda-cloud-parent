package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 离线卡数据查询
 * <p>
 * 帧类型码：0x48
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：查询桩本地存储的离线卡数据
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "48", name = "离线卡数据查询", description = "离线卡数据查询命令")
public class YkcV20OfflineCardQueryDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 查询起始卡号 (8字节)
     * HEX码
     */
    @ProtocolField(order = 2, length = 8, dataType = ProtocolDataType.HEX, description = "查询起始卡号")
    private String startCardNo;
}
