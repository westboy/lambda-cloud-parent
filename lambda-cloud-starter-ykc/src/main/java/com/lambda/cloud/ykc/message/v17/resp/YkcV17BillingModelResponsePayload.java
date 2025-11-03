package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议计费模型响应消息
 * <p>
 * 对应协议帧类型 0x0A，计费模型请求应答
 * 传送间隔：应答发送
 * 功能：运营平台对计费模型请求的应答
 * </p>
 *
 * @author Jin
 */
public class YkcV17BillingModelResponsePayload extends YkcV17BasePayload<YkcV17BillingModelResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型响应消息体
     */
    public YkcV17BillingModelResponsePayload(YkcV17BillingModelResponseDetail body) {
        this.setFrameType("0A");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17BillingModelResponsePayload() {
        this.setFrameType("0A");
    }

    static {
        ResponseBuilder.register("0A", "1.7", protocolMessage -> {
            YkcV17BillingModelResponsePayload billingModelResponseMessage = new YkcV17BillingModelResponsePayload();
            billingModelResponseMessage.setStartFlag("68");
            billingModelResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return billingModelResponseMessage;
        });
    }
}
