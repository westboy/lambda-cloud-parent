package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 运营平台确认启动充电响应消息
 * <p>
 * 帧类型码：0xA6
 * 传送间隔：应答
 * 功能：运营平台确认启动充电
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20StartChargingConfirmResponseMessage
        extends YkcV20BaseMessage<YkcV20StartChargingConfirmResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20StartChargingConfirmResponseMessage() {
        super();
        setFrameType("A6");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 运营平台确认启动充电响应详细信息
     */
    public YkcV20StartChargingConfirmResponseMessage(YkcV20StartChargingConfirmResponseDetail detail) {
        super();
        setFrameType("A6");
        setDetail(detail);
    }
}
