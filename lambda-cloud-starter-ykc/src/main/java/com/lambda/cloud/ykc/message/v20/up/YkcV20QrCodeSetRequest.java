package com.lambda.cloud.ykc.message.v20.up;

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
@ProtocolPayload(frameType = "5B", name = "二维码设置", description = "平台下发充电枪对应的二维码到桩")
public class YkcV20QrCodeSetRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "二维码码制")
    private Integer qrCodeType;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "二维码长度")
    private Integer qrCodeLength;

    @ProtocolField(
            order = 5,
            length = 1,
            dataType = ProtocolDataType.LIST,
            listElementType = ProtocolDataType.UINT8,
            listElementSizeField = "qrCodeLength",
            description = "二维码内容")
    private List<Integer> qrCodeContent;
}
