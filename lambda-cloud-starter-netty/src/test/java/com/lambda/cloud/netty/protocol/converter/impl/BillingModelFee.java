package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ProtocolFrame(frameType = "0A", name = "计费模型请求应答", description = "计费模型请求应答详细信息")
public class BillingModelFee {
    /**
     * 电费费率列表
     * 每个费率4字节，精确到五位小数
     */
    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX, precision = 5, description = "电费费率")
    private BigDecimal electricityRate;

    /**
     * 服务费费率列表
     * 每个费率4字节，精确到五位小数
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.HEX, precision = 5, description = "服务费费率")
    private BigDecimal serviceRate;
}
