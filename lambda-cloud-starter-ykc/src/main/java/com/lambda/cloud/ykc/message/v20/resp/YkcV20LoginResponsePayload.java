package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 登录认证应答响应消息
 * <p>
 * 帧类型码：0x02
 * 传送间隔：应答发送
 * 功能：回复电桩登陆结果
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20LoginResponsePayload extends YkcV20BasePayload<YkcV20LoginResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20LoginResponsePayload() {
        super();
        setFrameType("02");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 登录应答详细信息
     */
    public YkcV20LoginResponsePayload(YkcV20LoginResponseDetail detail) {
        super();
        setFrameType("02");
        setDetail(detail);
    }
}
