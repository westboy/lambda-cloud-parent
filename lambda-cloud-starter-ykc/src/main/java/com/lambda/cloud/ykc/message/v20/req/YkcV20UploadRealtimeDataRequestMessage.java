package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 上传实时监测数据响应消息
 * <p>
 * 帧类型码：0x13
 * 传送间隔：周期上送、变位上送、召唤
 * 功能：上送充电枪实时数据，周期上送时，待机5分钟、充电15秒
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20UploadRealtimeDataRequestMessage extends YkcV20BaseMessage<YkcV20UploadRealtimeDataRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20UploadRealtimeDataRequestMessage() {
        super();
        setFrameType("13");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 上传实时监测数据响应详细信息
     */
    public YkcV20UploadRealtimeDataRequestMessage(YkcV20UploadRealtimeDataRequestDetail detail) {
        super();
        setFrameType("13");
        setDetail(detail);
    }
}
