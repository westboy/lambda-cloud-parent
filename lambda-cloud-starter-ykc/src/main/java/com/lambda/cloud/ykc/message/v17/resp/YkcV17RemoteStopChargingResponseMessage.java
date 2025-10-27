package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议远程停机命令回复消息
 * <p>
 * 对应协议帧类型 0x35，远程停机命令回复
 * </p>
 *
 * @author Jin
 */
public class YkcV17RemoteStopChargingResponseMessage extends YkcV17BaseMessage<YkcV17RemoteStopChargingResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17RemoteStopChargingResponseMessage() {
        this.setFrameType("35");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17RemoteStopChargingResponseMessage(YkcV17RemoteStopChargingResponseDetail body) {
        this.setFrameType("35");
        this.setDetail(body);
    }

    static {
        ResponseBuilder.register("35", "1.7", protocolMessage -> {
            YkcV17RemoteStopChargingResponseMessage remoteStopChargingResponseMessage =
                    new YkcV17RemoteStopChargingResponseMessage();
            remoteStopChargingResponseMessage.setStartFlag("68");
            remoteStopChargingResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return remoteStopChargingResponseMessage;
        });
    }
}
