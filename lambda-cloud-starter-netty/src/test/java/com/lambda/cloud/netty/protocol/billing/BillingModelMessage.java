package com.lambda.cloud.netty.protocol.billing;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.message.ProtocolMessage;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ProtocolPayload(frameType = "0A", name = "计费模型请求应答", description = "计费模型请求应答详细信息")
public class BillingModelMessage implements ProtocolMessage {
    @Override
    public String getFrameType() {
        return "";
    }

    @Override
    public void setFrameType(String frameType) {}

    @Override
    public Integer getSerialNumber() {
        return 0;
    }

    @Override
    public void setSerialNumber(Integer serialNumber) {}

    /**
     * 复合字段List
     * 电费费率列表
     * 每个费率4字节，精确到五位小数
     */
    @ProtocolField(
            order = 1,
            listElementSize = 1,
            composite = true,
            dataType = ProtocolDataType.LIST,
            description = "电费费率")
    private List<BillingModelFee> fees;

    /**
     * 普通字段List
     * 时段费率号列表 (48字节)
     * 每半小时一个时段，共48个时段
     * 0x01：第1个费率，0x02：第2个费率，...，0x30：第48个费率
     */
    @ProtocolField(
            order = 6,
            length = 1,
            listElementSize = 3,
            listElementType = ProtocolDataType.HEX,
            dataType = ProtocolDataType.LIST,
            description = "时段费率号")
    private List<Integer> timeSlotRateNumbers;
}
