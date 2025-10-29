package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型验证请求消息
 * <p>
 * 帧类型码：0x05
 * 传送间隔：主动请求，直到成功
 * 功能：充电桩在登陆成功后，都需要对当前计费模型校验，如计费模型与平台当前不一致，则需要向平台请求新的计费模型
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20BillingModelVerificationRequestMessage
        extends YkcV20BaseMessage<YkcV20BillingModelVerificationRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20BillingModelVerificationRequestMessage() {
        super();
        setFrameType("05");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 计费模型验证请求详细信息
     */
    public YkcV20BillingModelVerificationRequestMessage(YkcV20BillingModelVerificationRequestDetail detail) {
        super();
        setFrameType("05");
        setDetail(detail);
    }
}
