package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩心跳包请求消息
 * <p>
 * 帧类型码：0x03
 * 传送间隔：10秒周期上送
 * 功能：用于链路状态判断，3次未收到心跳包视为网络异常，需要重新登陆
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20HeartbeatRequestMessage extends YkcV20BaseMessage<YkcV20HeartbeatRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20HeartbeatRequestMessage() {
        super();
        setFrameType("03");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 心跳包请求详细信息
     */
    public YkcV20HeartbeatRequestMessage(YkcV20HeartbeatRequestDetail detail) {
        super();
        setFrameType("03");
        setDetail(detail);
    }
}
