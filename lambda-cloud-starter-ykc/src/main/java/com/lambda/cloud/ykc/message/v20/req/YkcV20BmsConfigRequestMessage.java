package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
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
public class YkcV20BmsConfigRequestMessage extends YkcV20BaseMessage<YkcV20BmsConfigRequestDetail> {

    public YkcV20BmsConfigRequestMessage() {
        super();
        setFrameType("17");
    }

    public YkcV20BmsConfigRequestMessage(YkcV20BmsConfigRequestDetail detail) {
        setFrameType("17");
        setDetail(detail);
    }
}
