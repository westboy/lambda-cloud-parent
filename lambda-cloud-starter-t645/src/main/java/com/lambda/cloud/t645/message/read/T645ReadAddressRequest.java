package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 读通信地址请求报文。
 *
 * <p>控制码 {@code 0x13}，DI {@code C0320000}。
 * 主站请求读取表计的通信地址（6 字节 BCD 编码）。</p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "13:C0320000", name = "读通信地址请求")
public class T645ReadAddressRequest implements T645DataBody {

    /** 读通信地址 DI，固定为 {@code C0320000}。 */
    public static final String DI = "C0320000";

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    @Override
    public String getDi() {
        return di;
    }
}
