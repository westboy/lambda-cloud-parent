package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.1.0协议 - 计费模型请求应答
 * <p>
 * 帧类型码：0x0A
 * 对应协议文档：6.8 计费模型请求应答
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：用户充电费用计算，每半小时为一个费率段，共48段
 * </p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0A", name = "计费模型请求应答", description = "计费模型请求应答详细信息")
public class YkcV20BillingModelDown {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "费率数量")
    private Integer feeCount;

    @ProtocolField(
            order = 4,
            length = 8,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20BillingModelFee.class,
            listElementSizeField = "feeCount",
            description = "费率列表")
    private List<YkcV20BillingModelFee> fees;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "计损比例")
    private Integer lossRatio;

    @ProtocolField(
            order = 6,
            length = 1,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.UINT8,
            listElementSize = 48,
            description = "时段费率号数组")
    private List<Integer> timeSlotRates;
}
