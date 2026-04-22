package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(name = "计费模型费率")
public class YkcV20BillingModelFee {

    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "电费费率")
    private BigDecimal electricityRate;

    @ProtocolField(
            order = 2,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "服务费费率")
    private BigDecimal serviceRate;
}
