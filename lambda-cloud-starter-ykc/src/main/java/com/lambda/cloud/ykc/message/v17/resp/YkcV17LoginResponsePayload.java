package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议登录响应消息
 * <p>
 * 对应协议帧类型 0x02，充电桩登录认证响应
 * </p>
 *
 * @author Jin
 */
public class YkcV17LoginResponsePayload extends YkcV17BasePayload<YkcV17LoginResponseDetail> {

    public YkcV17LoginResponsePayload(YkcV17LoginResponseDetail body) {
        setFrameType("02");
        setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     *
     */
    public YkcV17LoginResponsePayload() {
        this.setFrameType("02");
    }

    static {
        ResponseBuilder.register("02", "1.7", protocolMessage -> {
            YkcV17LoginResponsePayload loginResponseMessage = new YkcV17LoginResponsePayload();
            loginResponseMessage.setStartFlag("68");
            loginResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return loginResponseMessage;
        });
    }
}
