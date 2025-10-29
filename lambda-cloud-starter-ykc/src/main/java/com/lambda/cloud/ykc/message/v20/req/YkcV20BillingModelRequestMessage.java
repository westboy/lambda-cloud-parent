package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩计费模型请求消息
 * <p>
 * 帧类型码：0x09
 * 传送间隔：主动请求，直到成功
 * 功能：充电桩向平台请求计费模型
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20BillingModelRequestMessage extends YkcV20BaseMessage<YkcV20BillingModelRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20BillingModelRequestMessage() {
        super();
        setFrameType("09");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 充电桩计费模型请求详细信息
     */
    public YkcV20BillingModelRequestMessage(YkcV20BillingModelRequestDetail detail) {
        super();
        setFrameType("09");
        setDetail(detail);
    }
}
