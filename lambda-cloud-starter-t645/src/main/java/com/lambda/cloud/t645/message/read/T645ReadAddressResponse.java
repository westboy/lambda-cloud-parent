package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645CodecSupport;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 读通信地址应答报文。
 *
 * <p>控制码 {@code 0x93}，DI {@code C0320000}。
 * 表计返回自身的通信地址（6 字节 BCD 编码，低字节在前）。</p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "93:C0320000", name = "读通信地址应答")
public class T645ReadAddressResponse implements T645DataBody {

    /** 读通信地址 DI，固定为 {@code C0320000}。 */
    public static final String DI = "C0320000";

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    /**
     * 通信地址（6 字节 BCD，低字节在前）。
     * 需调用 {@link T645CodecSupport#reverseAddress(String)} 翻转后获取实际地址。
     */
    @ProtocolField(order = 1, length = 6, dataType = ProtocolDataType.BCD, computed = true, description = "通信地址")
    private String address;

    @Override
    public String getDi() {
        return di;
    }
}
