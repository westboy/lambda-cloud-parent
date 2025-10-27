package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议登录响应消息体
 * <p>
 * 对应协议帧类型 0x02，充电桩登录认证响应的消息体部分
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x02", name = "登录响应", description = "充电桩登录认证响应消息体", version = "1.7")
public class YkcV17LoginResponseDetail {

    /**
     * 桩编码 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String stationCode;

    /**
     * 登陆结果 (1字节)
     * BIN码: 0x00 登陆成功, 0x01 登陆失败
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "登陆结果")
    private Integer loginResult;
}
