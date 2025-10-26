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
     * 桩编号 (7字节)
     */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.HEX, description = "桩编号")
    private String stationCode;

    /**
     * 登录结果 (1字节)
     * 0x00: 登录成功
     * 0x01: 用户名或密码错误
     * 0x02: 充电桩未注册
     * 0x03: 充电桩已被禁用
     * 0x04: 系统维护中
     * 0x05: 网络连接异常
     * 0x06: 其他错误
     */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.HEX, description = "登录结果")
    private Integer loginResult;
}
