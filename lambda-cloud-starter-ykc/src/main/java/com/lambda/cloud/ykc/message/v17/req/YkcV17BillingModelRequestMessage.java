package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议计费模型请求消息
 * <p>
 * 对应协议帧类型 0x09，充电桩计费模型请求
 * 传送间隔：主动请求
 * 功能：充电桩向运营平台请求计费模型
 * </p>
 *
 * @author Jin
 */
public class YkcV17BillingModelRequestMessage extends YkcV17BaseMessage<YkcV17BillingModelRequestDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 计费模型请求消息体
     */
    public YkcV17BillingModelRequestMessage(YkcV17BillingModelRequestDetail body) {
        this.setFrameType("09");
        this.setBody(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17BillingModelRequestMessage() {
        this.setFrameType("09");
    }
}
