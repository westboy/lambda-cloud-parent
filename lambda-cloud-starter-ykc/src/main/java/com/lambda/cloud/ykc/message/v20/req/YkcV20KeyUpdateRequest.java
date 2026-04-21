package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "96", name = "密钥更新", description = "运营平台下发RSA公钥到充电桩")
public class YkcV20KeyUpdateRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "密钥长度")
    private Integer keyLength;

    @ProtocolField(
            order = 3,
            length = 1,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.UINT8,
            listElementSizeField = "keyLength",
            description = "最新密钥")
    private List<Integer> latestKey;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executeControl;
}

