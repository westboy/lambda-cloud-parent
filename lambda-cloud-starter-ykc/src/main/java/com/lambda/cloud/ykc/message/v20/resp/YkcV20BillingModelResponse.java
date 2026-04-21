package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型请求应答详细信息
 * <p>
 * 帧类型码：0x0A
 * 对应协议文档：6.8 计费模型请求应答
 * 功能：用户充电费用计算，每半小时为一个费率段，共48段，每段对应48费率其中一个费率
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0A", name = "计费模型请求应答", description = "计费模型请求应答详细信息")
public class YkcV20BillingModelResponse {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 计费模型编号 (2字节)
     * 固定值：01 00
     */
    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;

    /**
     * 费率数量 (1字节)
     * 最多48个
     */
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

    /**
     *  普通字段List
     * 时段费率号列表 (48字节)
     * 每半小时一个时段，共48个时段
     * 0x01：第1个费率，0x02：第2个费率，...，0x30：第48个费率
     */
    @ProtocolField(
            order = 6,
            length = 1,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.UINT8,
            listElementSize = 48,
            description = "时段费率号")
    private List<Integer> timeSlotRateNumbers;
}
