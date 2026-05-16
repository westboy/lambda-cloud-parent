package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易费率时段
 * <p>
 * 对应协议文档：8.8 交易记录确认
 * 功能：用于描述交易记录中的费率时段信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "交易费率时段")
public class YkcV20TransactionRatePeriod {

    /**
     * 费率单价 (4字节)
     * BIN码，精确到小数点后五位
     */
    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "费率单价")
    private Integer unitPrice;

    /**
     * 费率电量 (4字节)
     * BIN码，精确到小数点后四位
     */
    @ProtocolField(
            order = 2,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率电量")
    private Integer energy;

    /**
     * 费率计损电量 (4字节)
     * BIN码，精确到小数点后四位
     */
    @ProtocolField(
            order = 3,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率计损电量")
    private Integer energyWithLoss;

    /**
     * 费率金额 (4字节)
     * BIN码，精确到小数点后四位
     */
    @ProtocolField(
            order = 4,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "费率金额")
    private Integer amount;
}
