package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议计费模型验证请求消息
 * <p>
 * 对应协议帧类型 0x05，充电桩向运营平台发送计费模型验证请求
 * </p>
 *
 * @author Jin
 */
public class YkcV17BillingModelVerificationRequestPayload
        extends YkcV17BasePayload<YkcV17BillingModelVerificationRequestDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型验证请求消息体
     */
    public YkcV17BillingModelVerificationRequestPayload(YkcV17BillingModelVerificationRequestDetail body) {
        this.setFrameType("05");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17BillingModelVerificationRequestPayload() {
        this.setFrameType("05");
    }
}
