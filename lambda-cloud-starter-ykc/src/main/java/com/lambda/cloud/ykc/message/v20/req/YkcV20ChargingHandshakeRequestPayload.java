package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充V2.0协议充电握手响应消息
 * <p>
 * 对应协议帧类型 0x15，GBT-27930充电桩与BMS充电握手阶段报文
 * </p>
 *
 * @author Jin
 */
@ToString(callSuper = true)
@Getter
@Setter
public class YkcV20ChargingHandshakeRequestPayload extends YkcV20BasePayload<YkcV20ChargingHandshakeRequestDetail> {

    public YkcV20ChargingHandshakeRequestPayload() {
        super();
        setFrameType("15");
    }

    public YkcV20ChargingHandshakeRequestPayload(YkcV20ChargingHandshakeRequestDetail detail) {
        setFrameType("15");
        setDetail(detail);
    }
}
