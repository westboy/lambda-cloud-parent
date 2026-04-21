package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YkcV20HalfHourEnergy {

    @ProtocolField(order = 1, length = 4, dataType = ProtocolDataType.UINT32, littleEndian = true, precision = 4, description = "电量")
    private Integer energy;
}

