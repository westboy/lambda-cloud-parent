package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议心跳响应消息
 * <p>
 * 对应协议帧类型 0x04，心跳包应答
 * 传送间隔：应答发送
 * 功能：运营平台对充电桩心跳包的应答
 * </p>
 *
 * @author Jin
 */
public class YkcV17HeartbeatResponsePayload extends YkcV17BasePayload<YkcV17HeartbeatResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 心跳响应消息体
     */
    public YkcV17HeartbeatResponsePayload(YkcV17HeartbeatResponseDetail body) {
        this.setFrameType("04");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17HeartbeatResponsePayload() {
        this.setFrameType("04");
    }

    static {
        ResponseBuilder.register("04", "1.7", protocolMessage -> {
            YkcV17HeartbeatResponsePayload heartbeatResponseMessage = new YkcV17HeartbeatResponsePayload();
            heartbeatResponseMessage.setStartFlag("68");
            heartbeatResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return heartbeatResponseMessage;
        });
    }
}
