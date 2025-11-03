package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BasePayload;

/**
 * 云快充1.7协议交易记录确认消息体
 * <p>
 * 对应协议帧类型 0x40，运营平台确认收到充电桩上传的交易记录
 * </p>
 *
 * @author Jin
 */
public class YkcV17TransactionRecordResponsePayload extends YkcV17BasePayload<YkcV17TransactionRecordResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17TransactionRecordResponsePayload() {
        this.setFrameType("40");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17TransactionRecordResponsePayload(YkcV17TransactionRecordResponseDetail body) {
        this.setFrameType("40");
        this.setDetail(body);
    }

    static {
        ResponseBuilder.register("40", "1.7", protocolMessage -> {
            YkcV17TransactionRecordResponsePayload transactionRecordConfirmResponseMessage =
                    new YkcV17TransactionRecordResponsePayload();
            transactionRecordConfirmResponseMessage.setStartFlag("68");
            transactionRecordConfirmResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return transactionRecordConfirmResponseMessage;
        });
    }
}
