package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;

/**
 * 云快充2.0协议 - 错误报文响应消息
 * <p>
 * 帧类型码：0x1B
 * 对应协议文档：GBT-27930充电桩与BMS充电错误报文
 * </p>
 *
 * @author zx
 */
public class YkcV20BmsErrorRequestMessage extends YkcV20BaseMessage<YkcV20BmsErrorRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20BmsErrorRequestMessage() {
        super();
        this.setFrameType("1B");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 错误报文响应详细信息
     */
    public YkcV20BmsErrorRequestMessage(YkcV20BmsErrorRequestDetail detail) {
        this.setFrameType("1B");
        this.setDetail(detail);
    }
}
