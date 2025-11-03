package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议读取实时监测数据请求消息
 * <p>
 * 对应协议帧类型 0x12，读取实时监测数据
 * 传送间隔：主动请求
 * 功能：运营平台根据需要主动发起读取实时数据的请求
 * </p>
 *
 * @author Jin
 */
public class YkcV17MonitoringDataRequestPayload extends YkcV17BasePayload<YkcV17MonitoringDataRequestDetail> {

    /**
     * 带消息体的构造函数
     *
     * @param body 读取实时监测数据请求消息体
     */
    public YkcV17MonitoringDataRequestPayload(YkcV17MonitoringDataRequestDetail body) {
        this.setFrameType("12");
        this.setDetail(body);
    }

    /**
     * 不带消息体的构造函数
     */
    public YkcV17MonitoringDataRequestPayload() {
        this.setFrameType("12");
    }
}
