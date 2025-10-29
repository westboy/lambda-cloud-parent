package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩登录认证请求消息
 * <p>
 * 帧类型码：0x01
 * 传送间隔：通信中断后上电复位
 * 功能：充电桩将桩设置的运营编码上传给运营平台，以实现运营平台将运营编码与充电桩建立连接关系
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20LoginRequestMessage extends YkcV20BaseMessage<YkcV20LoginRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20LoginRequestMessage() {
        super();
        setFrameType("01");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 登录请求详细信息
     */
    public YkcV20LoginRequestMessage(YkcV20LoginRequestDetail detail) {
        super();
        setFrameType("01");
        setDetail(detail);
    }
}
