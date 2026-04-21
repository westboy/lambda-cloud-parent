package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 余额更新应答详细信息
 *
 * <p>帧类型码：0x41</p>
 * <p>对应协议文档：8.10 余额更新应答</p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "41", name = "余额更新应答", description = "桩接收平台余额更新后的应答")
public class YkcV20AccountBalanceUpdateResponse {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 8, optional = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "修改结果")
    private Integer updateResult;
}

