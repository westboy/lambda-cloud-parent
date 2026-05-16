package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型费率项
 * <p>
 * 对应协议文档：6.8 计费模型请求应答
 * 功能：用于描述每半小时为一个费率段的电费信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "计费模型费率")
public class YkcV20BillingModelFee {

    /**
     * 电费费率 (4字节)
     * BIN码，精确到小数点后五位
     */
    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "电费费率")
    private BigDecimal electricityRate;

    /**
     * 服务费费率 (4字节)
     * BIN码，精确到小数点后五位
     */
    @ProtocolField(
            order = 2,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 5,
            description = "服务费费率")
    private BigDecimal serviceRate;
}
