package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
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
public class YkcV20ChargingHandshakeRequestMessage extends YkcV20BaseMessage<YkcV20ChargingHandshakeRequestDetail> {

    public YkcV20ChargingHandshakeRequestMessage() {
        super();
        this.frameType = "15";
    }

    public YkcV20ChargingHandshakeRequestMessage(YkcV20ChargingHandshakeRequestDetail detail) {
        super(detail);
        this.frameType = "15";
    }
}
