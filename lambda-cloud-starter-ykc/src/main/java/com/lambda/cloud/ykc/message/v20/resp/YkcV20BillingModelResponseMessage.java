package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型请求应答消息
 * <p>
 * 帧类型码：0x0A
 * 传送间隔：应答
 * 功能：平台对充电桩计费模型请求的应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20BillingModelResponseMessage extends YkcV20BaseMessage<YkcV20BillingModelResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20BillingModelResponseMessage() {
        super();
        setFrameType("0A");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 计费模型请求应答详细信息
     */
    public YkcV20BillingModelResponseMessage(YkcV20BillingModelResponseDetail detail) {
        super();
        setFrameType("0A");
        setDetail(detail);
    }
}
