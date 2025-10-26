package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议运营平台确认启动充电消息
 * <p>
 * 对应协议帧类型 0x32，运营平台确认启动充电
 * </p>
 *
 * @author Jin
 */
public class YkcV17StartChargingResponseMessage extends YkcV17BaseMessage<YkcV17StartChargingResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17StartChargingResponseMessage() {
        setFrameType("32");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17StartChargingResponseMessage(YkcV17StartChargingResponseDetail body) {
        setFrameType("32");
        setDetail(body);
    }
    
    static {
        ResponseBuilder.register("32", "1.7", protocolMessage -> {
            YkcV17StartChargingResponseMessage startChargingResponseMessage = new YkcV17StartChargingResponseMessage();
            startChargingResponseMessage.setStartFlag("68");
            startChargingResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return startChargingResponseMessage;
        });
    }
}