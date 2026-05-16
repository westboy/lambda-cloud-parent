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
 * 云快充2.0协议 - 计费模型请求应答
 * <p>
 * 帧类型码：0x0A
 * 对应协议文档：6.8 计费模型请求应答
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：用户充电费用计算，每半小时为一个费率段，共48段
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "0A", name = "计费模型请求应答", description = "计费模型请求应答详细信息")
public class YkcV20BillingModelDown {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 计费模型编号 (2字节)
     * BCD码，固定值：01 00
     */
    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;

    @ProtocolField(order = 3, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "开始时间")
    private String startTime;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "费率数量")
    private Integer feeCount;

    @ProtocolField(order = 5, length = 1, dataType = ProtocolDataType.UINT8, description = "服务费费率数量")
    private Integer serviceFeeCount;

    @ProtocolField(order = 6, length = 1, dataType = ProtocolDataType.UINT8, description = "停车费数量")
    private Integer parkingFeeCount;

    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "状态变化原因")
    private Integer statusChangeReason;

    @ProtocolField(order = 8, length = 255, composite = true, dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE, listElementClass = YkcV20BillingModelFee.class,
            listElementSizeField = "feeCount", description = "费率列表")
    private List<YkcV20BillingModelFee> fee;

    @ProtocolField(order = 9, length = 255, composite = true, dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE, listElementClass = YkcV20BillingModelFee.class,
            listElementSizeField = "serviceFeeCount", description = "服务费列表")
    private List<YkcV20BillingModelFee> serviceFee;

    @ProtocolField(order = 10, length = 255, composite = true, dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.COMPOSITE, listElementClass = YkcV20BillingModelFee.class,
            listElementSizeField = "parkingFeeCount", description = "停车费列表")
    private List<YkcV20BillingModelFee> parkingFee;
}
