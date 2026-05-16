package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 二维码设置
 * <p>
 * 帧类型码：0x5B
 * 对应协议文档：9.9 二维码设置
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：平台下发充电枪对应的二维码到桩，桩在屏幕上按内容生成二维码
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "5B", name = "二维码设置", description = "二维码设置命令")
public class YkcV20QrCodeIssueDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码，0x01枪1，0x02枪2，以此类推
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 二维码码制 (1字节)
     * 0x00 QR码，默认
     * 0x01汉信码
     * 0x02 PDF417
     * 0x03 Data Matrix
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "二维码码制")
    private Integer qrCodeFormat;

    /**
     * 二维码长度 (1字节)
     * BIN码
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "二维码长度")
    private Integer qrCodeLength;

    /**
     * 二维码内容 (N字节)
     * ASCII码
     */
    @ProtocolField(order = 5, length = 128, dataType = ProtocolDataType.ASCII, description = "二维码内容")
    private String qrCode;
}