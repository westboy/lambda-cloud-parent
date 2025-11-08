package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议登录请求消息
 * <p>
 * 对应协议帧类型 0x01，充电桩登录认证请求
 * </p>
 *
 * @author Jin
 */
public class YkcV17LoginRequestPayload extends YkcV17BasePayload<YkcV17LoginRequest> {
    /**
     * 带消息体的构造函数
     *
     * @param body 登录请求消息体
     */
    public YkcV17LoginRequestPayload(YkcV17LoginRequest body) {
        this.setFrameType("01");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     *
     */
    public YkcV17LoginRequestPayload() {
        this.setFrameType("01");
    }
}
