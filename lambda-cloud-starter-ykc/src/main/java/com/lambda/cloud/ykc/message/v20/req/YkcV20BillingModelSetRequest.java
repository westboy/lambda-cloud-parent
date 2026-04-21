package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "58", name = "计费模型设置", description = "运营平台下发计费模型（费率与时段费率号）")
public class YkcV20BillingModelSetRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编码")
    private String billingModelCode;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "费率数量")
    private Integer rateCount;

    @ProtocolField(
            order = 4,
            length = 8,
            composite = true,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE,
            listElementClass = YkcV20BillingModelFee.class,
            listElementSizeField = "rateCount",
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
            description = "时段费率号")
    private List<Integer> timeSlotRateNumbers;
}

