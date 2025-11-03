package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程启动充电命令回复响应消息
 * <p>
 * 帧类型码：0xA7
 * 传送间隔：应答
 * 功能：远程启动充电命令回复
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20RemoteStartReplyResponsePayload extends YkcV20BasePayload<YkcV20RemoteStartReplyResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20RemoteStartReplyResponsePayload() {
        super();
        setFrameType("A7");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 远程启动充电命令回复响应详细信息
     */
    public YkcV20RemoteStartReplyResponsePayload(YkcV20RemoteStartReplyResponseDetail detail) {
        super();
        setFrameType("A7");
        setDetail(detail);
    }
}
