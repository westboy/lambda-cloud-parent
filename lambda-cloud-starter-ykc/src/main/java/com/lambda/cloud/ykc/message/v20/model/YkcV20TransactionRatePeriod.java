package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(name = "交易费率时段")
public class YkcV20TransactionRatePeriod {

    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "费率单价")
    private Integer unitPrice;

    @ProtocolField(
            order = 2,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率电量")
    private Integer energy;

    @ProtocolField(
            order = 3,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率计损电量")
    private Integer energyWithLoss;

    @ProtocolField(
            order = 4,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率金额")
    private Integer amount;
}
