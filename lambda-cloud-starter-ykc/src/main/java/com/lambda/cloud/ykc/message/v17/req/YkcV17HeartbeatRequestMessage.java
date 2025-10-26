package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议心跳请求消息
 * <p>
 * 对应协议帧类型 0x03，充电桩心跳包
 * 传送间隔：10秒周期上送
 * 功能：用于链路状态判断，3次未收到心跳包视为网络异常，需要重新登陆
 * </p>
 *
 * @author Jin
 */
public class YkcV17HeartbeatRequestMessage extends YkcV17BaseMessage<YkcV17HeartbeatRequestDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 心跳请求消息体
     */
    public YkcV17HeartbeatRequestMessage(YkcV17HeartbeatRequestDetail body) {
        this.setFrameType("03");
        this.setBody(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17HeartbeatRequestMessage() {
        this.setFrameType("03");
    }
}
