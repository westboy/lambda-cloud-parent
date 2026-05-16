package com.lambda.cloud.ykc.message.v20.model;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 半小时电量信息
 * <p>
 * 对应协议文档：8.8 交易记录确认
 * 功能：用于描述每半小时的充电电量信息
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(name = "半小时电量")
public class YkcV20HalfHourEnergy {

    /**
     * 电量 (4字节)
     * BIN码，精确到小数点后四位
     */
    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            precision = 4,
            description = "电量")
    private Integer energy;
}
