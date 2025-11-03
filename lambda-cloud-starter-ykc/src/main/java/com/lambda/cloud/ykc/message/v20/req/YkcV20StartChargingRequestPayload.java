package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩主动申请启动充电请求消息
 * <p>
 * 帧类型码：0xA5
 * 传送间隔：主动请求
 * 功能：充电桩主动申请启动充电
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20StartChargingRequestPayload extends YkcV20BasePayload<YkcV20StartChargingRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20StartChargingRequestPayload() {
        super();
        setFrameType("A5");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 充电桩主动申请启动充电请求详细信息
     */
    public YkcV20StartChargingRequestPayload(YkcV20StartChargingRequestDetail detail) {
        super();
        setFrameType("A5");
        setDetail(detail);
    }
}
