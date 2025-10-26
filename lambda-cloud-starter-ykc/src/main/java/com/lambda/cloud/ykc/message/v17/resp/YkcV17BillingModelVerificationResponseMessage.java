package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议计费模型验证应答消息
 * <p>
 * 对应协议帧类型 0x06，运营平台对计费模型验证请求的应答
 * </p>
 *
 * @author Jin
 */
public class YkcV17BillingModelVerificationResponseMessage extends YkcV17BaseMessage<YkcV17BillingModelVerificationResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型验证应答消息体
     */
    public YkcV17BillingModelVerificationResponseMessage(YkcV17BillingModelVerificationResponseDetail body) {
        this.setFrameType("06");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17BillingModelVerificationResponseMessage() {
        this.setFrameType("06");
    }
    
    static {
        ResponseBuilder.register("06", "1.7", protocolMessage -> {
            YkcV17BillingModelVerificationResponseMessage verificationResponseMessage = new YkcV17BillingModelVerificationResponseMessage();
            verificationResponseMessage.setStartFlag("68");
            verificationResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return verificationResponseMessage;
        });
    }
}