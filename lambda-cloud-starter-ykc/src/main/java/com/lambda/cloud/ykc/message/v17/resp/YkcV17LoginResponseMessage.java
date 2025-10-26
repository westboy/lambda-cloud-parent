package com.lambda.cloud.ykc.message.v17.resp;


import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议登录响应消息
 * <p>
 * 对应协议帧类型 0x02，充电桩登录认证响应
 * </p>
 *
 * @author Generated
 */
public class YkcV17LoginResponseMessage extends YkcV17BaseMessage<YkcV17LoginResponseDetail> {


    public YkcV17LoginResponseMessage(YkcV17LoginResponseDetail body) {
        setFrameType("02");
        setBody(body);
    }

    /**
     * 不带消息体的构造函数
     *
     */
    public YkcV17LoginResponseMessage() {
        this.setFrameType("02");
    }

}
