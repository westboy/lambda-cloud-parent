package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议运营平台远程停机消息
 * <p>
 * 对应协议帧类型 0x36，运营平台远程停机
 * </p>
 *
 * @author Jin
 */
public class YkcV17RemoteStopChargingRequestMessage extends YkcV17BaseMessage<YkcV17RemoteStopChargingRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17RemoteStopChargingRequestMessage() {
        this.setFrameType("36");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17RemoteStopChargingRequestMessage(YkcV17RemoteStopChargingRequestDetail body) {
        this.setFrameType("36");
        this.setDetail(body);
    }
}