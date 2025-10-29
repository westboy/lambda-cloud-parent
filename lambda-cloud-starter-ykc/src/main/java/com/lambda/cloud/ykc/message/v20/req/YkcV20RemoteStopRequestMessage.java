package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台远程停机请求消息
 * <p>
 * 帧类型码：0x36
 * 传送间隔：按需发送
 * 功能：当用户通过远程停止充电时，发送本命令
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20RemoteStopRequestMessage extends YkcV20BaseMessage<YkcV20RemoteStopRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20RemoteStopRequestMessage() {
        super();
        setFrameType("36");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 运营平台远程停机请求详细信息
     */
    public YkcV20RemoteStopRequestMessage(YkcV20RemoteStopRequestDetail detail) {
        super();
        setFrameType("36");
        setDetail(detail);
    }
}
