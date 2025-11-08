package com.lambda.cloud.ykc.message.v16;

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
public class YkcV16BillingModelResponsePayload extends YkcV16BasePayload {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型响应消息体
     */
    public YkcV16BillingModelResponsePayload(YkcV16BillingModelResponseDetail body) {
        this.setFrameType("0A");
        //        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV16BillingModelResponsePayload() {
        this.setFrameType("0A");
    }
}
