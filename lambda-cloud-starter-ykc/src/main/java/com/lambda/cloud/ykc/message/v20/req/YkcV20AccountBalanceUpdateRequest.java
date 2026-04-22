package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程账户余额更新详细信息
 *
 * <p>帧类型码：0x42</p>
 * <p>对应协议文档：8.9 远程账户余额更新</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "42", name = "远程账户余额更新", description = "平台下发用户更新的余额到充电桩")
public class YkcV20AccountBalanceUpdateRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    @ProtocolField(
            order = 4,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            description = "修改后账户金额")
    private Long updatedAccountBalance;
}
