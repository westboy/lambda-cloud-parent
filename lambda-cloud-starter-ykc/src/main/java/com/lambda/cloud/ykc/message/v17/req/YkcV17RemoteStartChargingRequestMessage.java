package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议运营平台远程控制启机消息
 * <p>
 * 对应协议帧类型 0x34，运营平台远程控制启机
 * </p>
 *
 * @author Jin
 */
public class YkcV17RemoteStartChargingRequestMessage extends YkcV17BaseMessage<YkcV17RemoteStartChargingRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17RemoteStartChargingRequestMessage() {
        this.setFrameType("34");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17RemoteStartChargingRequestMessage(YkcV17RemoteStartChargingRequestDetail body) {
        this.setFrameType("34");
        this.setDetail(body);
    }
}