package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 读取实时监测数据请求消息
 * <p>
 * 帧类型码：0x12
 * 传送间隔：主动请求
 * 功能：运营平台根据需要主动发起读取实时数据的请求
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20ReadRealtimeDataRequestPayload extends YkcV20BasePayload<YkcV20ReadRealtimeDataRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20ReadRealtimeDataRequestPayload() {
        super();
        setFrameType("12");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 读取实时监测数据请求详细信息
     */
    public YkcV20ReadRealtimeDataRequestPayload(YkcV20ReadRealtimeDataRequestDetail detail) {
        super();
        setFrameType("12");
        setDetail(detail);
    }
}
