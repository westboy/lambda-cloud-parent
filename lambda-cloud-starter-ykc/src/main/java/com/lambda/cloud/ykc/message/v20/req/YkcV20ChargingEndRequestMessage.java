package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;

/**
 * 云快充2.0协议 - 充电结束响应消息
 * <p>
 * 帧类型码：0x19
 * 对应协议文档：GBT-27930充电桩与BMS充电结束阶段报文
 * </p>
 *
 * @author zx
 */
public class YkcV20ChargingEndRequestMessage extends YkcV20BaseMessage<YkcV20ChargingEndRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20ChargingEndRequestMessage() {
        super();
        this.setFrameType("19");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 充电结束响应详细信息
     */
    public YkcV20ChargingEndRequestMessage(YkcV20ChargingEndRequestDetail detail) {
        this.setFrameType("19");
        this.setDetail(detail);
    }
}
