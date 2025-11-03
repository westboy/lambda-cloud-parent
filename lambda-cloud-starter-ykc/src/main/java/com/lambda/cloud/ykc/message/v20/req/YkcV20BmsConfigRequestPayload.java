package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.0协议参数配置响应消息
 * <p>
 * 对应协议帧类型 0x17，GBT-27930充电桩与BMS参数配置阶段报文
 * </p>
 *
 * @author Jin
 */
@ToString(callSuper = true)
@Getter
@Setter
public class YkcV20BmsConfigRequestPayload extends YkcV20BasePayload<YkcV20BmsConfigRequestDetail> {

    public YkcV20BmsConfigRequestPayload() {
        super();
        setFrameType("17");
    }

    public YkcV20BmsConfigRequestPayload(YkcV20BmsConfigRequestDetail detail) {
        setFrameType("17");
        setDetail(detail);
    }
}
