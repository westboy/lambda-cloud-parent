package com.lambda.cloud.ykc.message.v17.req;


import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议登录请求消息
 * <p>
 * 对应协议帧类型 0x01，充电桩登录认证请求
 * </p>
 *
 * @author Generated
 */
public class YkcV17LoginRequestMessage extends YkcV17BaseMessage<YkcV17LoginRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17LoginRequestMessage() {
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 登录请求消息体
     */
    public YkcV17LoginRequestMessage(YkcV17LoginRequestDetail body) {
        setFrameType("01");
        setBody(body);
    }
}
