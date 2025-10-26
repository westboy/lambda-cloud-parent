package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

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
public class YkcV17HeartbeatResponseMessage extends YkcV17BaseMessage<YkcV17HeartbeatResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 心跳响应消息体
     */
    public YkcV17HeartbeatResponseMessage(YkcV17HeartbeatResponseDetail body) {
        this.setFrameType("04");
        this.setBody(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17HeartbeatResponseMessage() {
        this.setFrameType("04");
    }
}
