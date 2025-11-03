package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议远程启动充电命令回复消息
 * <p>
 * 对应协议帧类型 0x33，远程启动充电命令回复
 * </p>
 *
 * @author Jin
 */
public class YkcV17RemoteStartChargingResponsePayload
        extends YkcV17BasePayload<YkcV17RemoteStartChargingResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17RemoteStartChargingResponsePayload() {
        this.setFrameType("33");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17RemoteStartChargingResponsePayload(YkcV17RemoteStartChargingResponseDetail body) {
        this.setFrameType("33");
        this.setDetail(body);
    }

    static {
        ResponseBuilder.register("33", "1.7", protocolMessage -> {
            YkcV17RemoteStartChargingResponsePayload remoteStartChargingResponseMessage =
                    new YkcV17RemoteStartChargingResponsePayload();
            remoteStartChargingResponseMessage.setStartFlag("68");
            remoteStartChargingResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return remoteStartChargingResponseMessage;
        });
    }
}
