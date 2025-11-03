package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 心跳包应答响应消息
 * <p>
 * 帧类型码：0x04
 * 传送间隔：应答发送
 * 功能：用于链路状态判断
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20HeartbeatResponsePayload extends YkcV20BasePayload<YkcV20HeartbeatResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20HeartbeatResponsePayload() {
        super();
        setFrameType("04");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 心跳包应答详细信息
     */
    public YkcV20HeartbeatResponsePayload(YkcV20HeartbeatResponseDetail detail) {
        super();
        setFrameType("04");
        setDetail(detail);
    }
}
