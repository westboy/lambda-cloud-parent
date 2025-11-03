package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 计费模型验证请求应答消息
 * <p>
 * 帧类型码：0x06
 * 传送间隔：应答
 * 功能：平台对充电桩计费模型验证请求的应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20BillingModelVerificationResponsePayload
        extends YkcV20BasePayload<YkcV20BillingModelVerificationResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20BillingModelVerificationResponsePayload() {
        super();
        setFrameType("06");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 计费模型验证请求应答详细信息
     */
    public YkcV20BillingModelVerificationResponsePayload(YkcV20BillingModelVerificationResponseDetail detail) {
        super();
        setFrameType("06");
        setDetail(detail);
    }
}
