package com.lambda.cloud.ykc.message.v16;

import com.lambda.cloud.ykc.message.ResponseBuilder;

/**
 * 云快充1.6协议计费模型响应消息
 * <p>
 * 对应协议帧类型 0x0A，计费模型请求应答
 * 传送间隔：应答发送
 * 功能：运营平台对计费模型请求的应答
 * </p>
 *
 * @author Jin
 */
public class YkcV16BillingModelResponseMessage extends YkcV16BaseMessage<YkcV16BillingModelResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型响应消息体
     */
    public YkcV16BillingModelResponseMessage(YkcV16BillingModelResponseDetail body) {
        this.setFrameType("0A");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV16BillingModelResponseMessage() {
        this.setFrameType("0A");
    }

    static {
        ResponseBuilder.register("0A", "1.7", protocolMessage -> {
            YkcV16BillingModelResponseMessage billingModelResponseMessage = new YkcV16BillingModelResponseMessage();
            billingModelResponseMessage.setStartFlag("68");
            billingModelResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return billingModelResponseMessage;
        });
    }
}
