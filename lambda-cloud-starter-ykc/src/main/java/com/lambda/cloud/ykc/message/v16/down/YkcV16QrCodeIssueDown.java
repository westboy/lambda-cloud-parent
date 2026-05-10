package com.lambda.cloud.ykc.message.v16.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 - 后台远程下发二维码前缀指令
 * <p>
 * 对应协议帧类型 0xF0（平台->充电桩）
 * </p>
 * <p>
 * 说明：二维码格式有2种（以下前缀为 www.baidu.com? No=）
 * <ul>
 *   <li>0x00：二维码前缀+14位桩编号，如：www.baidu.com? No=3422001000233</li>
 *   <li>0x01：二维码前缀+14位桩编号+2位枪编号，如：(A枪) www.baidu.com? No=342200100023301、(B枪) www.baidu.com? No=342200100023302</li>
 * </ul>
 * 如果是单枪充电桩，使用A枪二维码。
 * </p>
 * <p>
 * 二维码下发时，只需下发前缀，同时选择是第0种格式还是第1种格式即可。
 * 如果二维码格式为0种，桩自动补充桩编号；如果二维码格式为1种，桩自动补充桩编号+2位枪编号。
 * </p>
 * <p>
 * 注册通过后，后台即可立即下发二维码。桩断电，二维码不保存，需要重新下发。
 * 推荐每次注册通过后，均下发一次二维码。每个桩下发一次前缀即可，无须按照枪个数下发。
 * </p>
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xF0", name = "后台远程下发二维码前缀指令", description = "后台远程下发二维码前缀指令", version = "1.6")
public class YkcV16QrCodeIssueDown {

    /**
     * 桩编码 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

    /**
     * 二维码格式 (1字节)
     * BIN码：0x00 第一种（前缀+桩编号），0x01 第二种（前缀+桩编号+枪编号）
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "二维码格式")
    private Integer qrCodeFormat;

    /**
     * 二维码前缀长度 (1字节)
     * BIN码，二维码前缀长度最大不超过200字节
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "二维码前缀长度")
    private Integer qrCodePrefixLength;

    /**
     * 二维码前缀 (可变长度)
     * ASCII码，如："www.baidu.com? No="
     */
    @ProtocolField(order = 4, computed = true, dataType = ProtocolDataType.ASCII, description = "二维码前缀")
    private String qrCodePrefix;
}
