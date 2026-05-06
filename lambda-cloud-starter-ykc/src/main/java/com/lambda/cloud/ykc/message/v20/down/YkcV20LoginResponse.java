package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 登录认证应答响应详细信息
 * <p>
 * 帧类型码：0x02
 * 对应协议文档：6.2 登录认证应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "02", name = "登录认证应答", description = "登录认证应答详细信息")
public class YkcV20LoginResponse {

    /**
     * 桩编码 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String equipmentId;

    /**
     * 登陆结果 (1字节)
     * 0x00：登陆成功
     * 0x01：登陆失败
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "登陆结果")
    private Integer loginResult;

    /**
     * 密钥长度 (1字节)
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "密钥长度")
    private Integer keyLength;

    /**
     * 最新密钥 (N字节)
     * RSA配置公钥
     */
    @ProtocolField(order = 4, dataType = ProtocolDataType.ASCII, description = "最新密钥")
    private String latestKey;
}
