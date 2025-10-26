package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议充电桩主动申请启动充电消息
 * <p>
 * 对应协议帧类型 0x3D，充电桩主动申请启动充电
 * </p>
 *
 * @author Jin
 */
public class YkcV17TransactionRecordRequestMessage extends YkcV17BaseMessage<YkcV17TransactionRecordRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17TransactionRecordRequestMessage() {
        this.setFrameType("3D");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17TransactionRecordRequestMessage(YkcV17TransactionRecordRequestDetail body) {
        this.setFrameType("3D");
        this.setDetail(body);
    }
}