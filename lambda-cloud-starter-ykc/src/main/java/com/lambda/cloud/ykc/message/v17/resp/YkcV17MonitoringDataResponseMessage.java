package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议上传实时监测数据响应消息
 * <p>
 * 对应协议帧类型 0x13，上传实时监测数据
 * 传送间隔：周期上送、变位上送、召唤
 * 功能：上送充电枪实时数据，周期上送时，待机5分钟、充电15秒
 * </p>
 *
 * @author Jin
 */
public class YkcV17MonitoringDataResponseMessage extends YkcV17BaseMessage<YkcV17MonitoringDataResponseDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 上传实时监测数据响应消息体
     */
    public YkcV17MonitoringDataResponseMessage(YkcV17MonitoringDataResponseDetail body) {
        this.setFrameType("13");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17MonitoringDataResponseMessage() {
        this.setFrameType("13");
    }
    
    static {
        ResponseBuilder.register("13", "1.7", protocolMessage -> {
            YkcV17MonitoringDataResponseMessage monitoringDataResponseMessage = new YkcV17MonitoringDataResponseMessage();
            monitoringDataResponseMessage.setStartFlag("68");
            monitoringDataResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return monitoringDataResponseMessage;
        });
    }
}
