package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台远程控制启机请求消息
 * <p>
 * 帧类型码：0xA8
 * 传送间隔：按需发送
 * 功能：当用户通过远程启动充电时，发送本命令；本报文帧功率调节优先级高于默认最大功率下发的报文帧
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20RemoteStartRequestPayload extends YkcV20BasePayload<YkcV20RemoteStartRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20RemoteStartRequestPayload() {
        super();
        setFrameType("A8");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 运营平台远程控制启机请求详细信息
     */
    public YkcV20RemoteStartRequestPayload(YkcV20RemoteStartRequestDetail detail) {
        super();
        setFrameType("A8");
        setDetail(detail);
    }
}
