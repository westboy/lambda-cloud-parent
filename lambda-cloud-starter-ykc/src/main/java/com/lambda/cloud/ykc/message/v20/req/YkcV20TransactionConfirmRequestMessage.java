package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.ykc.message.v20.YkcV20BaseMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录确认请求消息
 * <p>
 * 帧类型码：0x40
 * 传送间隔：应答
 * 功能：交易记录确认
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20TransactionConfirmRequestMessage extends YkcV20BaseMessage<YkcV20TransactionConfirmRequestDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20TransactionConfirmRequestMessage() {
        super();
        setFrameType("40");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 交易记录确认请求详细信息
     */
    public YkcV20TransactionConfirmRequestMessage(YkcV20TransactionConfirmRequestDetail detail) {
        super();
        setFrameType("40");
        setDetail(detail);
    }
}
