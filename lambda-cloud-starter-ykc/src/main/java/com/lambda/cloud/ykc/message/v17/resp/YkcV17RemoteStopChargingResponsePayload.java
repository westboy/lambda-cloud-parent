package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议远程停机命令回复消息
 * <p>
 * 对应协议帧类型 0x35，远程停机命令回复
 * </p>
 *
 * @author Jin
 */
public class YkcV17RemoteStopChargingResponsePayload extends YkcV17BasePayload<YkcV17RemoteStopChargingResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17RemoteStopChargingResponsePayload() {
        this.setFrameType("35");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17RemoteStopChargingResponsePayload(YkcV17RemoteStopChargingResponseDetail body) {
        this.setFrameType("35");
        this.setDetail(body);
    }

    static {
        ResponseBuilder.register("35", "1.7", protocolMessage -> {
            YkcV17RemoteStopChargingResponsePayload remoteStopChargingResponseMessage =
                    new YkcV17RemoteStopChargingResponsePayload();
            remoteStopChargingResponseMessage.setStartFlag("68");
            remoteStopChargingResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return remoteStopChargingResponseMessage;
        });
    }
}
