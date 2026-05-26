package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 读当前组合有功总电能请求报文。
 *
 * <p>控制码 {@code 0x11}，DI {@code 00010000}。
 * 主站请求读取表计的当前组合有功总电能值。</p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "11:00010000", name = "读当前组合有功总电能请求")
public class T645ReadEnergyRequest implements T645DataBody {

    /** 读当前组合有功总电能 DI，固定为 {@code 00010000}。 */
    public static final String DI = "00010000";

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    @Override
    public String getDi() {
        return di;
    }
}
