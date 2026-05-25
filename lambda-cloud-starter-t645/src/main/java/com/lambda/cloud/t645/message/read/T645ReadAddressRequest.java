package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "13:C0320000", name = "读通信地址请求")
public class T645ReadAddressRequest implements T645DataBody {

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    @Override
    public String getDi() {
        return di;
    }
}
